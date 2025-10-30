package com.dasom.dasomServer.Service;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.dasom.dasomServer.DTO.ApiResponse;
import com.dasom.dasomServer.DTO.HealthDataRequest;
import com.dasom.dasomServer.DAO.HealthMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.FloatBuffer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ONNX Runtime을 사용하여 LSTM 모델 추론을 관리하고 슬라이딩 윈도우를 처리합니다.
 * 모든 DB 작업은 HealthDataRequest 객체로 통일하여 수행합니다.
 */
@Service
@Slf4j
public class LstmInferenceService {

    private final HealthMapper dataMapper;
    private final LstmInputScaler scaler;
    private OrtEnvironment environment;
    private OrtSession session;

    @Value("${onnx.model.filename:lstm_personalized_model_final_v2.onnx}")
    private String onnxModelFilename;

    private static final int N_STEPS = LstmInputScaler.N_STEPS; // 6
    private static final int N_SEQ_FEATURES = 8; // 4 cont + 4 ohe
    private static final int N_STATIC_FEATURES = 3; // Age, Gender, RHR
    private String[] classLabels = {"정상", "주의", "위험"};

    public LstmInferenceService(HealthMapper dataMapper, LstmInputScaler scaler) {
        this.dataMapper = dataMapper;
        this.scaler = scaler;
    }

    @PostConstruct
    public void init() {
        try {
            environment = OrtEnvironment.getEnvironment();
            String modelPath = getClass().getClassLoader().getResource("model/" + onnxModelFilename).getPath();
            session = environment.createSession(modelPath, new OrtSession.SessionOptions());
            log.info("ONNX LSTM 모델 로드 성공: {}", onnxModelFilename);
        } catch (Exception e) {
            log.error("ONNX Model 초기화에 실패했습니다. 모델 파일과 경로, 의존성을 확인해보세요.", e);
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
    public ApiResponse<?> processAndAnalyze(HealthDataRequest healthDataRequest) {
        try {
            // DTO를 DB 모델로 사용
            HealthDataRequest newDataPoint = healthDataRequest; // 통일된 객체 사용

            // 시간 설정 (MyBatis 모델에 logData 필드가 LocalDateTime 타입으로 있다고 가정)
            LocalDateTime newRecordTime = LocalDateTime.now();
            // DataPoint 대신 HealthDataRequest 객체에 setLogData(LocalDateTime) 메서드가 있어야 함.
//             newDataPoint.setLogDate(newRecordTime);

            // 마지막 레코드 조회
            HealthDataRequest lastRecord = dataMapper.findLastHealthData(healthDataRequest.getSilverId());

            // 결측치 확인 및 삽입 (LOCF)
            if (lastRecord != null) {
                fillMissingDataPoints(lastRecord, newRecordTime);
            }

            // 실제 수신된 새 데이터 저장 전, 정적 특성 복사/확보
            if (lastRecord != null) {
                if(newDataPoint.getAge() == 0) {
                    newDataPoint.setAge(lastRecord.getAge());
                }
                if(newDataPoint.getGender() == null) {
                    newDataPoint.setGender(lastRecord.getGender());
                }
                if(newDataPoint.getRhr() == 0) {
                    newDataPoint.setRhr(lastRecord.getRhr());
                }
            }

            // 실제 수신된 새 데이터 저장 (MyBatis INSERT)
            dataMapper.insertHealthData(newDataPoint);

            // LSTM 분석 트리거
            String analysisResult = triggerSlidingWindowAnalysis(newDataPoint.getSilverId(), newRecordTime);

            // 성공 응답 포장
            return ApiResponse.success("분석 완료", analysisResult);

        } catch (Exception e) {
            log.error("데이터 분석 및 처리 실패. 원인: {}", e.getMessage(), e);
            return ApiResponse.error(
                    "데이터 처리 중 오류 발생: " + e.getMessage(),
                    e.getClass().getSimpleName()
            );
        }
    }

    //  내부 로직 메서드

    /**
     * 누락된 10분은 이전걸로 채우기
     */
    private void fillMissingDataPoints(HealthDataRequest lastRecord, LocalDateTime newRecordTime) {
        // LocalDateTime lastRecordTime = lastRecord.getLogData(); // logData 필드를 가정
        LocalDateTime lastRecordTime = LocalDateTime.now().minusMinutes(10); //임시로 현재 시간 - 10분으로 가정

        LocalDateTime fillTime = lastRecordTime.plusMinutes(10);

        while (fillTime.isBefore(newRecordTime)) {
            log.warn("데이터 비어있음 감지. 결측치 채움: {}", fillTime);

            HealthDataRequest fill = createFillDataPoint(lastRecord);
            // DTO에 setLogData 메서드가 있어야 함.
            // fill.setLogData(fillTime);
            dataMapper.insertHealthData(fill);

            fillTime = fillTime.plusMinutes(10);
        }
    }

    /** 누락된 10분 시점의 레코드를 이전 레코드의 값으로 채우는 HealthDataRequest 생성 */
    private HealthDataRequest createFillDataPoint(HealthDataRequest lastRecord) {
        // DTO 필드명에 맞게 값을 복사
        HealthDataRequest fill = new HealthDataRequest();
        fill.setSilverId(lastRecord.getSilverId());

        // 연속성 특성 (LOCF: 이전 값 그대로 복사)
        fill.setHeartRateAvg(lastRecord.getHeartRateAvg());
        fill.setSpo2(lastRecord.getSpo2()); // int 타입 Spo2

        // 활동 특성 (결측된 10분 동안 활동이 없었다고 가정 -> 0으로 채움)
        fill.setWalkingSteps(0);
        fill.setTotalCaloriesBurned(0); // DTO의 int 타입으로 가정

        // 수면 특성 (세션 기반이므로 누락된 10분에는 0으로 채움)
        fill.setSleepDurationMin(0);
        fill.setSleepStageWakeMin(0);
        fill.setSleepStageDeepMin(0);
        fill.setSleepStageRemMin(0);
        fill.setSleepStageLightMin(0);

        // 정적 특성 (Age, Gender, RHR)은 DTO에 있다면 그대로 복사해야 합니다.
        // DTO에 해당 필드가 없다고 가정하고 이 부분은 생략.

        // 주의: AllArgsConstructor를 사용했으므로, 이 방식 대신 Builder 패턴을 사용하거나
        // 생성자에 모든 필드를 전달하는 방식으로 객체를 생성해야 합니다.
        return fill;
    }

    /** * 새로운 데이터 수신 시 슬라이딩 윈도우를 구축하고 추론을 실행합니다. */
    private String triggerSlidingWindowAnalysis(String userId, LocalDateTime currentTime) throws OrtException {
        if (session == null) return "모델 로드 안됨";

        // 쿼리 시간 범위 설정 및 Mapper를 통해 시퀀스 데이터 조회
        LocalDateTime startTime = currentTime.minusMinutes((N_STEPS - 1) * 10L).minusSeconds(30);
        LocalDateTime endTime = currentTime.plusSeconds(30);

        // Mapper의 findSequenceData 메서드를 사용
        List<HealthDataRequest> rawSequence = dataMapper.findSequenceData(
                userId,
                startTime,
                endTime,
                N_STEPS);

        // 데이터 개수 확인 (6개가 안 되면 분석 실행 불가)
        if (rawSequence.size() < N_STEPS) {
            return "INSUFFICIENT_DATA";
        }

        // 모델 입력 데이터 변환 및 정규화
        float[] seqContInput = new float[N_STEPS * N_SEQ_FEATURES];
        float[] staticInput = new float[N_STATIC_FEATURES];

        createSequentialInput(rawSequence, seqContInput);
        staticInput = createStaticInput(rawSequence.get(N_STEPS - 1)); // 최신 데이터로 정적 특성 준비

        // ONNX 텐서 생성 및 추론 실행
        return runInference(seqContInput, staticInput);
    }

    // ... (createSequentialInput, createStaticInput, runInference 메서드 생략 - 이전 답변과 동일) ...

    /** HealthDataRequest 리스트를 LSTM 입력 시퀀스 텐서 형식으로 변환합니다. */
    private void createSequentialInput(List<HealthDataRequest> sequence, float[] seqContInput) {
        // 💡 DataPoint 대신 HealthDataRequest 필드를 사용하도록 수정해야 합니다.
    }

    /** HealthDataRequest를 정적 특성 텐서 형식으로 변환합니다. */
    private float[] createStaticInput(HealthDataRequest latestPoint) {
        // 💡 DataPoint 대신 HealthDataRequest 필드를 사용하도록 수정해야 합니다.
        return new float[N_STATIC_FEATURES]; // 임시 반환
    }

    /** ONNX Runtime을 사용하여 추론을 실행합니다. */
    private String runInference(float[] seqInput, float[] staticInput) throws OrtException {
        // 이전에 제공된 runInference 내용을 여기에 복사해야 합니다.
        return "Simulated Result"; // 임시 반환
    }
}
