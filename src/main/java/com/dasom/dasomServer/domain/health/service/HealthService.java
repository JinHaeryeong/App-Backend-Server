package com.dasom.dasomServer.domain.health.service;

import com.dasom.dasomServer.domain.health.entity.DailyHealthLog;
import com.dasom.dasomServer.domain.health.entity.HealthLog;
import com.dasom.dasomServer.domain.health.repository.DailyHealthLogRepository;
import com.dasom.dasomServer.domain.health.repository.HealthLogRepository;
import com.dasom.dasomServer.shared.common.ApiResponse;
import com.dasom.dasomServer.domain.health.dto.DailyHealthLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthService {

    private final HealthLogRepository healthLogRepository;
    private final DailyHealthLogRepository dailyHealthLogRepository;


    // 공통 빌더 메서드
    private DailyHealthLog buildDailyHealthLog(String silverId, LocalDate logDate, DailyHealthLogRequest request, Long existingId) {
        return DailyHealthLog.builder()
                .id(existingId) // id가 null이면 신규 생성, 값이 있으면 업데이트로
                .silverId(silverId)
                .weight(request.getWeight())
                .bloodSugar(request.getBloodSugar())
                .bodyTemperature(request.getBodyTemperature())
                .sleepScore(request.getSleepScore())
                .systolicBloodPressure(request.getSystolicBloodPressure())
                .diastolicBloodPressure(request.getDiastolicBloodPressure())
                .logDate(logDate)
                .build();
    }
    @Transactional
    public ApiResponse<?> upsertDailyHealthLog(DailyHealthLogRequest summaryRequest) {
        String silverId = summaryRequest.getSilverId();
        java.time.LocalDate logDate = summaryRequest.getLogDate();

        try {
            // 기존 기록이 있는지 조회
            DailyHealthLog logEntry = dailyHealthLogRepository.findBySilverIdAndLogDate(silverId, logDate)
                    .map(existing -> buildDailyHealthLog(silverId, logDate, summaryRequest, existing.getId())) // 수정
                    .orElseGet(() -> buildDailyHealthLog(silverId, logDate, summaryRequest, null));

            // 저장
            dailyHealthLogRepository.save(logEntry);

            log.info("일일 건강 데이터 저장/업데이트 성공: {}", silverId);
            return ApiResponse.success("일일 건강 데이터 저장 완료", logEntry);

        } catch (Exception e) {
            log.error("일일 건강 데이터 처리 중 오류 발생: {}", silverId, e);
            return ApiResponse.error("서버 오류 발생: " + e.getMessage(), "SERVER_ERROR");
        }
    }

    // 최근 로그 6개 조회
    public List<HealthLog> findRecentLogs(String silverId) {
        return healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId);
    }
}
