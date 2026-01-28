package com.dasom.dasomServer.infra.ai;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.dasom.dasomServer.domain.health.entity.HealthLog;
import com.dasom.dasomServer.domain.health.entity.HealthResultLog;
import com.dasom.dasomServer.domain.health.entity.HealthStatus;
import com.dasom.dasomServer.domain.health.repository.HealthLogRepository;
import com.dasom.dasomServer.domain.health.repository.HealthResultLogRepository;
import com.dasom.dasomServer.domain.silver.entity.Silver;
import com.dasom.dasomServer.domain.silver.repository.SilverRepository;
import com.dasom.dasomServer.global.common.ApiResponse;
import com.dasom.dasomServer.domain.health.dto.HealthRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class LstmInferenceService {

    private final HealthLogRepository healthLogRepository;
    private final SilverRepository silverRepository;
    private final HealthResultLogRepository resultRepository;
    private final LstmInputScaler scaler;

    private OrtEnvironment environment;
    private OrtSession session;

    @Value("classpath:model/lstm_personalized_model_final_v2.onnx")
    private Resource onnxModelResource;

    private static final int N_STEPS = 6;
    private static final int N_SEQ_FEATURES = 8;
    private static final int N_STATIC_FEATURES = 3;
    private final double DEFAULT_RHR = 70.0;
    private final String[] classLabels = {"위험", "정상", "주의"};

    @PostConstruct
    public void init() {
        File modelFile = null;
        try {
            environment = OrtEnvironment.getEnvironment();
            modelFile = File.createTempFile("onnx_model", ".onnx");
            try (InputStream inputStream = onnxModelResource.getInputStream()) {
                Files.copy(inputStream, modelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            session = environment.createSession(modelFile.getAbsolutePath(), new OrtSession.SessionOptions());
            log.info("ONNX LSTM 모델 로드 성공");
            modelFile.deleteOnExit();
        } catch (Exception e) {
            log.error("ONNX Model 초기화 실패", e);
            throw new RuntimeException("ONNX 모델 로드 실패", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (session != null) session.close();
            if (environment != null) environment.close();
        } catch (OrtException e) {
            log.error("ONNX 런타임 클린업 실패", e);
        }
    }

    // save도 하고 count도 하니까
    @Transactional
    public ApiResponse<?> processAndAnalyze(HealthRequest dto) {
        String silverId = dto.getSilverId();
        try {
            LocalDateTime now = LocalDateTime.now();

            // 코파일럿이 지적한 부분 값이 null인지 체크 해줘야 nullPointerException 안터짐
            HealthLog newLog = HealthLog.builder()
                    .silverId(silverId)
                    .heartRate(dto.getHeartRateAvg() != null ? dto.getHeartRateAvg().intValue() : 0)
                    .stepCount(dto.getWalkingSteps())
                    .caloriesBurned(dto.getTotalCaloriesBurned())
                    .oxygen(dto.getSpo2() > 0 ? (double) dto.getSpo2() : null)
                    .logDate(dto.getLogDate() != null ? dto.getLogDate() : now)
                    .totalSleepMin(dto.getSleepDurationMin() != null ? dto.getSleepDurationMin().intValue() : 0)
                    .deepMin(dto.getSleepStageDeepMin() != null ? dto.getSleepStageDeepMin().intValue() : 0)
                    .lightMin(dto.getSleepStageLightMin() != null ? dto.getSleepStageLightMin().intValue() : 0)
                    .remMin(dto.getSleepStageRemMin() != null ? dto.getSleepStageRemMin().intValue() : 0)
                    .wakeMin(dto.getSleepStageWakeMin() != null ? dto.getSleepStageWakeMin().intValue() : 0)
                    .build();

            healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId).stream()
                    .findFirst()
                    .ifPresent(lastLog -> fillMissingDataPoints(lastLog, now));

            healthLogRepository.save(newLog);

            /* 
            
            수정함: count 값 변수에 담아서 쿼리 횟수 줄이기 (성능 최적화용) 
            기존 코드는 DB 서버 갔다가 결과 가져오는 과정이 두번임 
            고친 코드는 long count = healthLogRepository.countBySilverId(silverId);
            에서 한번만 물어봄
            */
            long count = healthLogRepository.countBySilverId(silverId);
            if (count < N_STEPS) {
                return ApiResponse.success(String.format("데이터 축적 중 (%d/%d)", count, N_STEPS));
            }

            String analysisResult = triggerSlidingWindowAnalysis(silverId);
            return ApiResponse.success("분석 완료 및 결과 반환", analysisResult);

        } catch (Exception e) {
            log.error("데이터 처리 실패: {}", e.getMessage(), e);
            return ApiResponse.error("처리 중 오류 발생", e.getClass().getSimpleName());
        }
    }

    private void fillMissingDataPoints(HealthLog lastLog, LocalDateTime newRecordTime) {
        LocalDateTime lastRecordTime = lastLog.getLogDate();
        if (lastRecordTime == null) return;

        LocalDateTime fillTime = lastRecordTime.plusMinutes(10);
        while (fillTime.isBefore(newRecordTime.minusSeconds(1))) {
            HealthLog fill = HealthLog.builder()
                    .silverId(lastLog.getSilverId())
                    .heartRate(lastLog.getHeartRate())
                    .oxygen(lastLog.getOxygen())
                    .stepCount(0)
                    .caloriesBurned(0.0)
                    .logDate(fillTime)
                    .wakeMin(10)
                    .build();
            healthLogRepository.save(fill);
            fillTime = fillTime.plusMinutes(10);
        }
    }

    private String triggerSlidingWindowAnalysis(String silverId) throws OrtException {
        if (session == null) return "모델 로드 안됨";

        List<HealthLog> sequence = healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId);
        if (sequence.size() < N_STEPS) return "INSUFFICIENT_DATA";

        Collections.reverse(sequence);

        float[] seqContInput = new float[N_STEPS * N_SEQ_FEATURES];
        float[] staticInput = createStaticInput(silverId);

        for (int i = 0; i < N_STEPS; i++) {
            HealthLog logData = sequence.get(i);
            logData.prepareForAiInference(); // Boolean 플래그 세팅

            int idx = i * N_SEQ_FEATURES;
            double[] scaled = scaler.scaleSeqContFeatures(
                    logData.getHeartRate(), logData.getOxygen(),
                    logData.getStepCount(), logData.getCaloriesBurned());

            seqContInput[idx] = (float) scaled[0];
            seqContInput[idx + 1] = (float) scaled[1];
            seqContInput[idx + 2] = (float) scaled[2];
            seqContInput[idx + 3] = (float) scaled[3];
            seqContInput[idx + 4] = logData.isDeepSleep() ? 1.0f : 0.0f;
            seqContInput[idx + 5] = logData.isLightSleep() ? 1.0f : 0.0f;
            seqContInput[idx + 6] = logData.isRemSleep() ? 1.0f : 0.0f;
            seqContInput[idx + 7] = logData.isAwakeSleep() ? 1.0f : 0.0f;
        }

        return runInference(silverId, seqContInput, staticInput);
    }

    private float[] createStaticInput(String silverId) {
        // Optional로 안전하게 조회
        Silver silver = silverRepository.findByLoginId(silverId)
                .orElseThrow(() -> new RuntimeException("어르신을 찾을 수 없습니다: " + silverId));

        int age = (int) ChronoUnit.YEARS.between(silver.getBirthday().toLocalDate(), LocalDate.now());

        // char 타입 비교
        float genderValue = (silver.getGender() == 'M' || silver.getGender() == 'm') ? 1.0f : 0.0f;

        // RHR 처리
        double validRHR = (silver.getRhr() != null && silver.getRhr() > 0) ? silver.getRhr() : DEFAULT_RHR;
        double[] scaledStatic = scaler.scaleStaticFeatures(age, (int)genderValue, validRHR);
        float[] staticInput = new float[N_STATIC_FEATURES];
        IntStream.range(0, N_STATIC_FEATURES).forEach(i -> staticInput[i] = (float) scaledStatic[i]);
        return staticInput;
    }

    /** 수정! JPA HealthResultLogRepository 사용 */
    private String runInference(String silverId, float[] seqInput, float[] staticInput) throws OrtException {
        try (OnnxTensor seqTensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(seqInput), new long[]{1, N_STEPS, N_SEQ_FEATURES});
             OnnxTensor statTensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(staticInput), new long[]{1, N_STATIC_FEATURES})) {

            Map<String, OnnxTensor> inputs = Map.of("input_sequence", seqTensor, "input_static", statTensor);

            try (OrtSession.Result result = session.run(inputs)) {
                float[][] rawProbabilities = (float[][]) result.get(0).getValue();
                float[] probabilities = rawProbabilities[0];

                int predictedClass = 0;
                float maxProb = 0;
                for (int i = 0; i < probabilities.length; i++) {
                    if (probabilities[i] > maxProb) {
                        maxProb = probabilities[i];
                        predictedClass = i;
                    }
                }

                HealthStatus status = HealthStatus.values()[predictedClass];
                String label = status.name();
                log.info("추론 결과 저장: SilverId={}, Label={}", silverId, label);

                // JPA로 분석 결과 저장
                resultRepository.save(HealthResultLog.builder()
                        .silverId(silverId)
                        .label(status)
                        .logDate(LocalDateTime.now())
                        .build());

                return label;
            }
        }
    }
}