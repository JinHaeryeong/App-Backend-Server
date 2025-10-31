package com.dasom.dasomServer.Service;

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
import org.springframework.core.io.Resource; // Resource import
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    // 💡 수정: Resource 타입 주입 시, 경로 앞에 'classpath:' 프리픽스를 명시적으로 붙여야 합니다.
    @Value("classpath:model/lstm_personalized_model_final_v2.onnx")
    private Resource onnxModelResource;

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
        File modelFile = null;
        try {
            environment = OrtEnvironment.getEnvironment();

            // Resource에서 InputStream을 가져와 임시 파일로 복사
            modelFile = File.createTempFile("onnx_model", ".onnx");
            try (InputStream inputStream = onnxModelResource.getInputStream()) {
                Files.copy(inputStream, modelFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("ONNX 모델 파일을 임시 파일로 복사하는 데 실패했습니다.", e);
                throw e; // 복사 실패 시 즉시 예외 발생
            }

            // 임시 파일 경로를 사용하여 ONNX Session 생성
            session = environment.createSession(modelFile.getAbsolutePath(), new OrtSession.SessionOptions());
            log.info("ONNX LSTM 모델 로드 성공: {}", onnxModelResource.getFilename());

            // JVM 종료 시 임시 파일 삭제 예약
            modelFile.deleteOnExit();

        } catch (Exception e) {
            log.error("ONNX Model 초기화에 실패했습니다. 모델 파일과 경로, 의존성을 확인해보세요.", e);

            // 초기화 실패 시 임시 파일이 생성되었다면 삭제
            if (modelFile != null) {
                modelFile.delete();
            }
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

            Optional<HealthRequest> lastRecordOptional = dataMapper.findLastHealthData(silverId);

            if (lastRecordOptional.isPresent()) {
                HealthRequest lastRecord = lastRecordOptional.get();

                fillMissingDataPoints(lastRecord, newRecordTime);
            }

            dataMapper.insertHealthData(healthDataRequest); // DB의 CURRENT_TIMESTAMP에 의해 logDate 저장됨


            int currentCount = dataMapper.countBySilverId(silverId);

            if (currentCount < N_STEPS) {
                String message = String.format("데이터 저장 완료. LSTM 분석을 위해 %d개 데이터가 더 필요합니다 (현재 %d개).",
                        N_STEPS - currentCount, currentCount);
                return ApiResponse.success(message);
            }

            String analysisResult = triggerSlidingWindowAnalysis(silverId, newRecordTime); // newRecordTime을 기준으로 분석

            return ApiResponse.success("분석 완료 및 결과 반환", analysisResult);

        } catch (Exception e) {
            log.error("데이터 분석 및 처리 실패. 원인: {}", e.getMessage(), e);
            return ApiResponse.error("데이터 처리 중 오류 발생: " + e.getMessage(), e.getClass().getSimpleName());
        }
    }


    //  내부 로직 메서드

    /**
     * 누락된 10분은 이전걸로 채우기
     */
    private void fillMissingDataPoints(HealthRequest lastRecord, LocalDateTime newRecordTime) {
        LocalDateTime lastRecordTime = lastRecord.getLogDate();

        if (lastRecordTime == null) {
            log.error("마지막 레코드에 logDate가 없어 결측치 처리를 건너뜁니다.");
            return;
        }

        LocalDateTime fillTime = lastRecordTime.plusMinutes(10);

        while (fillTime.isBefore(newRecordTime.minusSeconds(1))) { // 1초 여유를 두어 현재 요청과 겹치지 않게 함
            log.warn("데이터 비어있음 감지. 결측치 채움: {}", fillTime);

            HealthRequest fill = createFillDataPoint(lastRecord);

            fill.setLogDate(fillTime); // 정확한 결측 시간 설정

            dataMapper.insertHealthData(fill); // DB에 삽입

            fillTime = fillTime.plusMinutes(10); // 다음 10분 간격으로 이동
        }
    }

    /** 누락된 10분 시점의 레코드를 이전 레코드의 값으로 채우는 HealthDataRequest 생성 */
    private HealthRequest createFillDataPoint(HealthRequest lastRecord) {
        HealthRequest fill = new HealthRequest();
        fill.setSilverId(lastRecord.getSilverId());

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


        return fill;
    }

    /** * 새로운 데이터 수신 시 슬라이딩 윈도우를 구축하고 추론을 실행 */
    private String triggerSlidingWindowAnalysis(String userId, LocalDateTime currentTime) throws OrtException {
        if (session == null) return "모델 로드 안됨";

        // 쿼리 시간 범위 설정 및 Mapper를 통해 시퀀스 데이터 조회
        LocalDateTime startTime = currentTime.minusMinutes((N_STEPS - 1) * 10L).minusSeconds(30);
        LocalDateTime endTime = currentTime.plusSeconds(30);

        // Mapper의 findSequenceData 메서드를 사용
        List<HealthRequest> rawSequence = dataMapper.findSequenceData(
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
    private void createSequentialInput(List<HealthRequest> sequence, float[] seqContInput) {
        // DataPoint 대신 HealthDataRequest 필드를 사용하도록 수정해야 합니다.
    }

    /** HealthDataRequest를 정적 특성 텐서 형식으로 변환합니다. */
    private float[] createStaticInput(HealthRequest latestPoint) {
        // DataPoint 대신 HealthDataRequest 필드를 사용하도록 수정해야 함
        return new float[N_STATIC_FEATURES]; // 임시 반환
    }

    /** ONNX Runtime을 사용하여 추론을 실행 */
    private String runInference(float[] seqInput, float[] staticInput) throws OrtException {
        // 이전에 제공된 runInference 내용을 여기에 복사
        return "Simulated Result"; // 임시 반환
    }
}
