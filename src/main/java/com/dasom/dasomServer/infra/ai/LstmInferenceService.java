package com.dasom.dasomServer.infra.ai;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.dasom.dasomServer.health.domain.*;
import com.dasom.dasomServer.health.presentation.dto.HealthDataRequest;
import com.dasom.dasomServer.silver.domain.Silver;
import com.dasom.dasomServer.silver.infrastructure.SilverRepository;
import com.dasom.dasomServer.shared.common.ApiResponse;
import com.dasom.dasomServer.shared.config.RabbitMqConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class LstmInferenceService {

    private final HealthLogRepository healthLogRepository;
    private final HealthResultLogRepository resultRepository;
    private final SilverRepository silverRepository;
    private final LstmInputScaler scaler;
    private final RabbitTemplate rabbitTemplate;

    private OrtEnvironment environment;
    private OrtSession session;

    @Value("classpath:model/lstm_personalized_model_final_v2.onnx")
    private Resource onnxModelResource;

    private static final int N_STEPS          = 6;
    private static final int N_SEQ_FEATURES   = 8;
    private static final int N_STATIC_FEATURES = 3;
    private static final double DEFAULT_RHR   = 70.0;

    @PostConstruct
    public void init() {
        try {
            environment = OrtEnvironment.getEnvironment();
            File modelFile = File.createTempFile("onnx_model", ".onnx");
            try (InputStream inputStream = onnxModelResource.getInputStream()) {
                Files.copy(inputStream, modelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            session = environment.createSession(modelFile.getAbsolutePath(), new OrtSession.SessionOptions());
            modelFile.deleteOnExit();
            log.info("ONNX LSTM 모델 로드 성공");
        } catch (Exception e) {
            log.error("ONNX 모델 초기화 실패", e);
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

    /**
     * 생체 신호 저장 및 비동기 AI 분석 파이프라인 트리거
     */
    @Transactional
    public ApiResponse<?> processAndAnalyze(HealthDataRequest request) {
        String silverId = request.getSilverId();
        try {
            HealthLog newLog = buildHealthLog(request);

            fillMissingDataPointsIfNeeded(silverId, newLog.getLogDate());
            healthLogRepository.save(newLog);

            long count = healthLogRepository.countBySilverId(silverId);
            if (count < N_STEPS) {
                return ApiResponse.success(String.format("데이터 축적 중 (%d/%d)", count, N_STEPS));
            }

            publishAnalysisEvent(silverId);
            return ApiResponse.success("데이터 수집 완료. 분석이 백그라운드에서 시작됩니다");

        } catch (Exception e) {
            log.error("데이터 처리 실패: silverId={}", silverId, e);
            return ApiResponse.error("처리 중 오류 발생", e.getClass().getSimpleName());
        }
    }

    /**
     * 슬라이딩 윈도우 기반 시계열 데이터 전처리 및 AI 추론 실행
     */
    @Transactional
    public void triggerSlidingWindowAnalysis(String silverId) throws OrtException {
        if (session == null) {
            log.error("AI 분석 실패: ONNX 세션 미초기화 - silverId={}", silverId);
            return;
        }

        List<HealthLog> sequence = healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId);
        if (sequence.size() < N_STEPS) {
            log.warn("AI 분석 스킵: 데이터 부족 ({}/{}), silverId={}", sequence.size(), N_STEPS, silverId);
            return;
        }

        Collections.reverse(sequence);

        float[] seqInput    = buildSequenceInput(sequence);
        float[] staticInput = buildStaticInput(silverId);

        runInference(silverId, seqInput, staticInput);
    }

    // -------------------------------------------------------------------------
    // private helpers
    // -------------------------------------------------------------------------

    private HealthLog buildHealthLog(HealthDataRequest request) {
        LocalDateTime logDate = request.getLogDate() != null
                ? request.getLogDate()
                : LocalDateTime.now();

        int heartRate = resolveHeartRate(request.getSilverId(), request.getHeartRateAvg());

        return HealthLog.from(
                request.getSilverId(),
                heartRate,
                request.getWalkingSteps(),
                request.getTotalCaloriesBurned(),
                request.getSpo2(),
                logDate,
                request.getSleepDurationMin(),
                request.getSleepStageDeepMin(),
                request.getSleepStageLightMin(),
                request.getSleepStageRemMin(),
                request.getSleepStageWakeMin()
        );
    }

    private int resolveHeartRate(String silverId, Long heartRateAvg) {
        if (heartRateAvg != null) {
            return heartRateAvg.intValue();
        }
        log.warn("심박수 데이터 누락 - 기본값 적용: silverId={}, defaultHr={}", silverId, (int) DEFAULT_RHR);
        return (int) DEFAULT_RHR;
    }

    private void fillMissingDataPointsIfNeeded(String silverId, LocalDateTime newRecordTime) {
        healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId)
                .stream()
                .findFirst()
                .ifPresent(lastLog -> fillGap(lastLog, newRecordTime));
    }

    private void fillGap(HealthLog lastLog, LocalDateTime newRecordTime) {
        if (lastLog.getLogDate() == null) return;

        LocalDateTime fillTime = lastLog.getLogDate().plusMinutes(10);
        while (fillTime.isBefore(newRecordTime.minusSeconds(1))) {
            healthLogRepository.save(HealthLog.filledFrom(lastLog, fillTime));
            fillTime = fillTime.plusMinutes(10);
        }
    }

    private void publishAnalysisEvent(String silverId) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend(RabbitMqConfig.HEALTH_ANALYSIS_QUEUE, silverId);
                }
            });
        } else {
            rabbitTemplate.convertAndSend(RabbitMqConfig.HEALTH_ANALYSIS_QUEUE, silverId);
        }
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

    private float[] buildStaticInput(String silverId) {
        Silver silver = silverRepository.findByLoginId(silverId)
                .orElseThrow(() -> new RuntimeException("어르신을 찾을 수 없습니다: " + silverId));

        int age = (int) ChronoUnit.YEARS.between(silver.getBirthday().toLocalDate(), LocalDate.now());
        float genderValue = (silver.getGender() == 'M' || silver.getGender() == 'm') ? 1.0f : 0.0f;
        double validRhr   = (silver.getRhr() != null && silver.getRhr() > 0) ? silver.getRhr() : DEFAULT_RHR;

        double[] scaledStatic = scaler.scaleStaticFeatures(age, (int) genderValue, validRhr);
        float[] staticInput = new float[N_STATIC_FEATURES];
        IntStream.range(0, N_STATIC_FEATURES).forEach(i -> staticInput[i] = (float) scaledStatic[i]);
        return staticInput;
    }

    private void runInference(String silverId, float[] seqInput, float[] staticInput) throws OrtException {
        try (OnnxTensor seqTensor  = OnnxTensor.createTensor(environment, FloatBuffer.wrap(seqInput),   new long[]{1, N_STEPS, N_SEQ_FEATURES});
             OnnxTensor statTensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(staticInput), new long[]{1, N_STATIC_FEATURES})) {

            Map<String, OnnxTensor> inputs = Map.of(
                    "input_sequence", seqTensor,
                    "input_static",   statTensor
            );

            try (OrtSession.Result result = session.run(inputs)) {
                HealthStatus status = extractPredictedStatus(result);
                log.info("AI 추론 완료: silverId={}, label={}", silverId, status);

                resultRepository.save(HealthResultLog.builder()
                        .silverId(silverId)
                        .label(status)
                        .logDate(LocalDateTime.now())
                        .build());
            }
        }
    }

    private HealthStatus extractPredictedStatus(OrtSession.Result result) throws OrtException {
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
        return HealthStatus.values()[predictedClass];
    }
}
