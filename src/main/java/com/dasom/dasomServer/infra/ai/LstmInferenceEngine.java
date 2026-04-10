package com.dasom.dasomServer.infra.ai;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.dasom.dasomServer.health.domain.HealthLog;
import com.dasom.dasomServer.health.domain.HealthStatus;
import com.dasom.dasomServer.silver.domain.Silver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.FloatBuffer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * ONNX 세션을 이용해 시퀀스 데이터를 추론하는 엔진.
 * 텐서 빌드, 모델 실행, 결과 해석만 담당한다.
 * 데이터 조회/저장 등 외부 상태 변경은 HealthAnalysisPipeline에서 처리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LstmInferenceEngine {

    private static final int N_STEPS           = 6;
    private static final int N_SEQ_FEATURES    = 8;
    private static final int N_STATIC_FEATURES = 3;
    private static final double DEFAULT_RHR    = 70.0;

    private final LstmModelSession modelSession;
    private final LstmInputScaler scaler;

    /**
     * 6개의 건강 로그 시퀀스와 어르신 정적 정보를 받아 건강 상태를 추론
     *
     * @param sequence 시간 오름차순으로 정렬된 HealthLog 6개
     * @param silver   정적 특성(나이, 성별, 안정 심박수) 제공용 어르신 엔티티
     * @return 예측된 HealthStatus
     */
    public HealthStatus infer(List<HealthLog> sequence, Silver silver) throws OrtException {
        float[] seqInput    = buildSequenceInput(sequence);
        float[] staticInput = buildStaticInput(silver);
        return runInference(seqInput, staticInput);
    }

    private float[] buildSequenceInput(List<HealthLog> sequence) {
        float[] seqInput = new float[N_STEPS * N_SEQ_FEATURES];

        for (int i = 0; i < N_STEPS; i++) {
            HealthLog log = sequence.get(i);
            log.prepareForAiInference();

            int idx = i * N_SEQ_FEATURES;
            double[] scaled = scaler.scaleSeqContFeatures(
                    log.getHeartRate(), log.getOxygen(),
                    log.getStepCount(), log.getCaloriesBurned());

            seqInput[idx]     = (float) scaled[0];
            seqInput[idx + 1] = (float) scaled[1];
            seqInput[idx + 2] = (float) scaled[2];
            seqInput[idx + 3] = (float) scaled[3];
            seqInput[idx + 4] = log.isDeepSleep()  ? 1.0f : 0.0f;
            seqInput[idx + 5] = log.isLightSleep() ? 1.0f : 0.0f;
            seqInput[idx + 6] = log.isRemSleep()   ? 1.0f : 0.0f;
            seqInput[idx + 7] = log.isAwakeSleep() ? 1.0f : 0.0f;
        }
        return seqInput;
    }

    private float[] buildStaticInput(Silver silver) {
        LocalDate birthday = silver.getBirthday() != null
                ? silver.getBirthday().toLocalDate()
                : LocalDate.of(1950, 1, 1);

        int age          = (int) ChronoUnit.YEARS.between(birthday, LocalDate.now());
        float genderVal  = (silver.getGender() == 'M' || silver.getGender() == 'm') ? 1.0f : 0.0f;
        double validRhr  = (silver.getRhr() != null && silver.getRhr() > 0) ? silver.getRhr() : DEFAULT_RHR;

        double[] scaledStatic = scaler.scaleStaticFeatures(age, (int) genderVal, validRhr);
        float[] staticInput   = new float[N_STATIC_FEATURES];
        IntStream.range(0, N_STATIC_FEATURES).forEach(i -> staticInput[i] = (float) scaledStatic[i]);
        return staticInput;
    }

    private HealthStatus runInference(float[] seqInput, float[] staticInput) throws OrtException {
        OrtSession session     = modelSession.getSession();
        OrtEnvironment env     = modelSession.getEnvironment();

        try (OnnxTensor seqTensor  = OnnxTensor.createTensor(env, FloatBuffer.wrap(seqInput),   new long[]{1, N_STEPS, N_SEQ_FEATURES});
             OnnxTensor statTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(staticInput), new long[]{1, N_STATIC_FEATURES})) {

            Map<String, OnnxTensor> inputs = Map.of(
                    "input_sequence", seqTensor,
                    "input_static",   statTensor
            );

            try (OrtSession.Result result = session.run(inputs)) {
                return extractStatus(result);
            }
        }
    }

    private HealthStatus extractStatus(OrtSession.Result result) throws OrtException {
        float[][] rawProbs = (float[][]) result.get(0).getValue();
        float[] probs      = rawProbs[0];

        int predictedClass = 0;
        float maxProb      = 0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb        = probs[i];
                predictedClass = i;
            }
        }
        return HealthStatus.values()[predictedClass];
    }
}
