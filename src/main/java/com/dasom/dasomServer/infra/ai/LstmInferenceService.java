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
import com.dasom.dasomServer.global.config.RabbitMqConfig;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class LstmInferenceService {

    private final HealthLogRepository healthLogRepository;
    private final SilverRepository silverRepository;
    private final HealthResultLogRepository resultRepository;
    private final LstmInputScaler scaler;
    private final RabbitTemplate rabbitTemplate;

    private OrtEnvironment environment;
    private OrtSession session;

    @Value("classpath:model/lstm_personalized_model_final_v2.onnx")
    private Resource onnxModelResource;

    private static final int N_STEPS = 6;
    private static final int N_SEQ_FEATURES = 8;
    private static final int N_STATIC_FEATURES = 3;
    private final double DEFAULT_RHR = 70.0;

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

    // 생체 신호 수집 및 비동기 분석 파이프라인 트리거
    @Transactional
    public ApiResponse<?> processAndAnalyze(HealthRequest dto) {
        String silverId = dto.getSilverId();
        try {
            LocalDateTime now = LocalDateTime.now();

            Integer heartRate = (dto.getHeartRateAvg() != null)
                    ? dto.getHeartRateAvg().intValue()
                    : null;

            if (heartRate == null) {
                // 이미 선언된 DEFAULT_RHR을 (int)로 캐스팅해서 사용
                heartRate = (int) DEFAULT_RHR;
                log.warn("SilverId: {} - 심박수 데이터 누락으로 기본값({}) 적용", silverId, heartRate);
            }


            // 코파일럿이 지적한 부분 값이 null인지 체크 해줘야 nullPointerException 안터짐
            HealthLog newLog = HealthLog.builder()
                    .silverId(silverId)
                    .heartRate(heartRate)
                    .stepCount(dto.getWalkingSteps())
                    .caloriesBurned(dto.getTotalCaloriesBurned())
                    .oxygen(dto.getSpo2() > 0 ? (double) dto.getSpo2() : 98.0)
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
            */
            long count = healthLogRepository.countBySilverId(silverId);
            if (count < N_STEPS) {
                return ApiResponse.success(String.format("데이터 축적 중 (%d/%d)", count, N_STEPS));
            }
            
            // 트랜잭션 커밋 완료 시점에 분석 메시지 발행 (이벤트 기반 비동기 처리)
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        rabbitTemplate.convertAndSend(RabbitMqConfig.HEALTH_ANALYSIS_QUEUE, silverId);
                    }
                });
            } else {
                // 혹시라도 트랜잭션이 없는 상태에서 호출될 경우를 대비한 방어 코드
                rabbitTemplate.convertAndSend(RabbitMqConfig.HEALTH_ANALYSIS_QUEUE, silverId);
            }

            return ApiResponse.success("데이터 수집 완료. 분석이 백그라운드에서 시작됩니다");
        } catch (Exception e) {
            log.error("데이터 처리 실패: {}", e.getMessage(), e);
            return ApiResponse.error("처리 중 오류 발생", e.getClass().getSimpleName());
        }
    }

    
    // 데이터 연속성 보장을 위한 결측치 보간
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


    // 슬라이딩 윈도우 기반의 시계열 데이터 전처리 및 분석 실행
    @Transactional
    public void triggerSlidingWindowAnalysis(String silverId) throws OrtException {
        if (session == null) {
            log.error("AI 분석 실패: ONNX 세션이 초기화되지 않았습니다.");
            return;
        }

        List<HealthLog> sequence = healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId);
        if (sequence.size() < N_STEPS) {
            log.warn("AI 분석 스킵: 데이터 부족 ({} / {})", sequence.size(), N_STEPS);
            return;
        }

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

        runInference(silverId, seqContInput, staticInput);
    }

    // 사용자 기본 정보(고정 피처) 전처리: 나이, 성별, RHR
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

    // 모델 돌리기
    private void runInference(String silverId, float[] seqInput, float[] staticInput) throws OrtException {
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
                log.info("추론 결과 저장: SilverId={}, Label={}", silverId, status);

                // JPA로 분석 결과 저장
                resultRepository.save(HealthResultLog.builder()
                        .silverId(silverId)
                        .label(status)
                        .logDate(LocalDateTime.now())
                        .build());
            }
        }
    }
}