package com.dasom.dasomServer.Service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.dasom.dasomServer.DTO.ApiResponse;
import com.dasom.dasomServer.DTO.HealthRequest;
import com.dasom.dasomServer.DAO.HealthMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
@Slf4j
public class LstmInferenceService {

    private final HealthMapper dataMapper;
    private final LstmInputScaler scaler; // 이 클래스는 정규화 로직이 있다고 가정
    private OrtEnvironment environment;
    private OrtSession session;

    @Value("classpath:model/lstm_personalized_model_final_v2.onnx")
    private Resource onnxModelResource;

    private static final int N_STEPS = 6;
    private static final int N_SEQ_FEATURES = 8;
    private static final int N_STATIC_FEATURES = 3;
    private final double DEFAULT_RHR = 70.0;
    private String[] classLabels = {"위험", "주의", "정상"};

    public LstmInferenceService(HealthMapper dataMapper, LstmInputScaler scaler) {
        this.dataMapper = dataMapper;
        this.scaler = scaler;
    }

    // ONNX 초기화 (생략 없이 최종 코드로 포함)
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
            log.info("ONNX LSTM 모델 로드 성공: {}", onnxModelResource.getFilename());
            modelFile.deleteOnExit();

        } catch (Exception e) {
            log.error("ONNX Model 초기화에 실패했습니다. 모델 파일과 경로, 의존성을 확인해보세요.", e);
            if (modelFile != null) modelFile.delete();
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
     * 새로운 데이터 수신 시 결측치를 채우고, 슬라이딩 윈도우를 구축하여 추론을 실행
     */
    public ApiResponse<?> processAndAnalyze(HealthRequest healthDataRequest) {
        String silverId = healthDataRequest.getSilverId();
        try {
            LocalDateTime newRecordTime = LocalDateTime.now();

            // Minute을 Boolean 플래그로 변환 (LSTM 요구사항)
            healthDataRequest.setDeepSleep(healthDataRequest.getSleepStageDeepMin() > 0);
            healthDataRequest.setRemSleep(healthDataRequest.getSleepStageRemMin() > 0);
            healthDataRequest.setLightSleep(healthDataRequest.getSleepStageLightMin() > 0);
            healthDataRequest.setAwakeSleep(healthDataRequest.getSleepStageWakeMin() > 0);

            Optional<HealthRequest> lastRecordOptional = dataMapper.findLastHealthData(silverId);

            if (lastRecordOptional.isPresent()) {
                HealthRequest lastRecord = lastRecordOptional.get();
                fillMissingDataPoints(lastRecord, newRecordTime);
            }

            dataMapper.insertHealthData(healthDataRequest);

            if (dataMapper.countBySilverId(silverId) < N_STEPS) {
                return ApiResponse.success(
                        String.format("데이터 저장 완료. LSTM 분석을 위해 %d개 데이터가 더 필요합니다.", N_STEPS - dataMapper.countBySilverId(silverId))
                );
            }

            String analysisResult = triggerSlidingWindowAnalysis(silverId, newRecordTime);
            return ApiResponse.success("분석 완료 및 결과 반환", analysisResult);

        } catch (Exception e) {
            log.error("데이터 분석 및 처리 실패. 원인: {}", e.getMessage(), e);
            return ApiResponse.error("데이터 처리 중 오류 발생: " + e.getMessage(), e.getClass().getSimpleName());
        }
    }

    //  내부 로직 메서드

    private void fillMissingDataPoints(HealthRequest lastRecord, LocalDateTime newRecordTime) {
        LocalDateTime lastRecordTime = lastRecord.getLogDate();
        if (lastRecordTime == null) return;

        LocalDateTime fillTime = lastRecordTime.plusMinutes(10);
        while (fillTime.isBefore(newRecordTime.minusSeconds(1))) {
            HealthRequest fill = createFillDataPoint(lastRecord);
            fill.setLogDate(fillTime);
            dataMapper.insertHealthData(fill);
            fillTime = fillTime.plusMinutes(10);
        }
    }

    /** 누락된 10분 시점의 레코드를 이전 레코드의 값으로 채우는 HealthDataRequest 생성 */
    private HealthRequest createFillDataPoint(HealthRequest lastRecord) {
        HealthRequest fill = new HealthRequest();
        fill.setSilverId(lastRecord.getSilverId());

        // LOCF: 이전 값을 그대로 복사
        fill.setHeartRateAvg(lastRecord.getHeartRateAvg());
        fill.setSpo2(lastRecord.getSpo2());

        // 활동/수면: 결측 시 활동/수면 없음 (0) 및 Boolean=False, 상태= NONE으로 채움
        fill.setWalkingSteps(0);
        fill.setTotalCaloriesBurned(0);
        fill.setSleepDurationMin(0L);
        fill.setSleepStageWakeMin(1L);
        fill.setSleepStageDeepMin(0L);
        fill.setSleepStageRemMin(0L);
        fill.setSleepStageLightMin(0L);

        fill.setDeepSleep(false);
        fill.setRemSleep(false);
        fill.setLightSleep(false);
        fill.setAwakeSleep(true);
        fill.setCurrentSleepStage("AWAKE"); // 미착용/데이터 없음 상태 명시

        return fill;
    }

    /** 새로운 데이터 수신 시 슬라이딩 윈도우를 구축하고 추론을 실행 */
    private String triggerSlidingWindowAnalysis(String silverId, LocalDateTime currentTime) throws OrtException {
        if (session == null) return "모델 로드 안됨";

        LocalDateTime startTime = currentTime.minusMinutes((N_STEPS - 1) * 10L).minusSeconds(30);
        LocalDateTime endTime = currentTime.plusSeconds(30);

        List<HealthRequest> rawSequence = dataMapper.findSequenceData(silverId, startTime, endTime, N_STEPS);
        if (rawSequence.size() < N_STEPS) return "INSUFFICIENT_DATA";

        float[] seqContInput = new float[N_STEPS * N_SEQ_FEATURES];
        float[] staticInput = createStaticInput(silverId); // silverId 기반 정적 특성 조회

        createSequentialInput(rawSequence, seqContInput);
        return runInference(silverId, seqContInput, staticInput);
    }

    /** 정적 특성 텐서(Age, Gender, RHR)를 구성 */
    private float[] createStaticInput(String silverId) {
        // Mapper에서 StaticUserInfo DTO 및 findUserInfo 메서드가 정의되어 있다고 가정
        HealthMapper.StaticUserInfo userInfo = dataMapper.findUserInfo(silverId);

        // Age 계산
        int age = (int) ChronoUnit.YEARS.between(userInfo.getBirthday(), LocalDate.now());

        // Gender 변환 ('M'/'F'를 1.0f/0.0f로)
        float genderValue = (userInfo.getGender() != null && userInfo.getGender().toUpperCase().startsWith("M")) ? 1.0f : 0.0f;

        // RHR 처리 및 정규화
        double validRHR = userInfo.getRhr();
        if (validRHR <= 0.0) {
            log.warn("Silver ID {}의 RHR이 0.0입니다. 기본값 {}을 사용합니다.", silverId, DEFAULT_RHR);
            validRHR = DEFAULT_RHR;
        }

        // 정적 특성 정규화
        double[] scaledStatic = scaler.scaleStaticFeatures(age, (int)genderValue, validRHR);

        float[] staticInput = new float[N_STATIC_FEATURES];
        IntStream.range(0, N_STATIC_FEATURES).forEach(i -> staticInput[i] = (float) scaledStatic[i]);

        return staticInput;
    }


    /** HealthDataRequest 리스트를 LSTM 입력 시퀀스 텐서 형식으로 변환 */
    private void createSequentialInput(List<HealthRequest> sequence, float[] seqContInput) {
        // LSTM 입력 시퀀스 배열 (6 steps x 8 features)을 채움
        for (int i = 0; i < N_STEPS; i++) {
            HealthRequest data = sequence.get(i);
            int startIdx = i * N_SEQ_FEATURES;

            // 연속형 4개 정규화 (Heartrate, SPO2, Steps, Calories)
            double[] scaledCont = scaler.scaleSeqContFeatures(
                    data.getHeartRateAvg(),
                    data.getSpo2(),
                    data.getWalkingSteps(),
                    data.getTotalCaloriesBurned()
            );

            // 정규화된 연속형 데이터 복사 (인덱스 0 ~ 3)
            for (int j = 0; j < scaledCont.length; j++) {
                seqContInput[startIdx + j] = (float) scaledCont[j];
            }

            // OHE (Boolean) 데이터 복사 (인덱스 4 ~ 7)
            // 순서: DEEP, LIGHT, REM, AWAKE (학습 코드 순서와 동일)
            seqContInput[startIdx + 4] = data.isDeepSleep() ? 1.0f : 0.0f;
            seqContInput[startIdx + 5] = data.isLightSleep() ? 1.0f : 0.0f;
            seqContInput[startIdx + 6] = data.isRemSleep() ? 1.0f : 0.0f;
            seqContInput[startIdx + 7] = data.isAwakeSleep() ? 1.0f : 0.0f;
        }
    }

    private String runInference(String silverId, float[] seqInput, float[] staticInput) throws OrtException {
        if (session == null) return "모델 로드 실패";

        OnnxTensor seqTensor = null;
        OnnxTensor statTensor = null;

        try {
            // 시퀀스 텐서 생성 (Batch Size=1, N_STEPS=6, N_SEQ_FEATURES=8)
            seqTensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(seqInput), new long[]{1, N_STEPS, N_SEQ_FEATURES});

            // 정적 텐서 생성 (Batch Size=1, N_STATIC_FEATURES=3)
            statTensor = OnnxTensor.createTensor(environment, FloatBuffer.wrap(staticInput), new long[]{1, N_STATIC_FEATURES});

            // ONNX 입력 이름 매핑 (학습 코드의 input_names와 동일해야 함)
            Map<String, OnnxTensor> inputs = Map.of(
                    "input_sequence", seqTensor,
                    "input_static", statTensor
            );

            // 모델 실행
            OrtSession.Result result = session.run(inputs);

            // 결과 해석 (소프트맥스 확률 값)
            float[][] rawProbabilities = (float[][]) result.get(0).getValue();
            float[] probabilities = rawProbabilities[0]; // 배치 크기 1

            // 최고 확률 클래스 찾기
            int predictedClass = 0;
            float maxProb = 0;
            for (int i = 0; i < probabilities.length; i++) {
                if (probabilities[i] > maxProb) {
                    maxProb = probabilities[i];
                    predictedClass = i;
                }
            }

            String label = classLabels[predictedClass];
            log.info("LSTM 추론 결과: Label={}, 확률={:.4f}", label, maxProb);

            dataMapper.insertAnalysisResult(silverId, label);
            return label;

        } catch (OrtException e) {
            log.error("ONNX 추론 실행 중 오류 발생", e);
            throw e;
        } finally {
            if (seqTensor != null) seqTensor.close();
            if (statTensor != null) statTensor.close();
        }
    }
}