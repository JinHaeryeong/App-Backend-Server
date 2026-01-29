package com.dasom.dasomServer.domain.health.service;

import com.dasom.dasomServer.domain.health.entity.DailyHealthLog;
import com.dasom.dasomServer.domain.health.entity.HealthLog;
import com.dasom.dasomServer.domain.health.repository.DailyHealthLogRepository;
import com.dasom.dasomServer.domain.health.repository.HealthLogRepository;
import com.dasom.dasomServer.global.common.ApiResponse;
import com.dasom.dasomServer.domain.health.dto.DailyHealthLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthService {

    private final HealthLogRepository healthLogRepository;
    private final DailyHealthLogRepository dailyHealthLogRepository;

    @Transactional
    public ApiResponse<?> upsertDailyHealthLog(DailyHealthLogRequest summaryRequest) {
        String silverId = summaryRequest.getSilverId();
        java.time.LocalDate logDate = summaryRequest.getLogDate();

        try {
            // 기존 기록이 있는지 조회
            DailyHealthLog logEntry = dailyHealthLogRepository.findBySilverIdAndLogDate(silverId, logDate)
                    .map(existing -> {
                        // 데이터가 있으면 업데이트 (Builder를 통해 기존 ID 유지하며 값 변경)
                        return DailyHealthLog.builder()
                                .id(existing.getId())
                                .silverId(silverId)
                                .weight(summaryRequest.getWeight())
                                .bloodSugar(summaryRequest.getBloodSugar())
                                .bodyTemperature(summaryRequest.getBodyTemperature())
                                .sleepScore(summaryRequest.getSleepScore())
                                .systolicBloodPressure(summaryRequest.getSystolicBloodPressure())
                                .diastolicBloodPressure(summaryRequest.getDiastolicBloodPressure())
                                .logDate(logDate)
                                .build();
                    })
                    .orElseGet(() -> {
                        // 데이터가 없으면 신규 생성
                        return DailyHealthLog.builder()
                                .silverId(silverId)
                                .weight(summaryRequest.getWeight())
                                .bloodSugar(summaryRequest.getBloodSugar())
                                .bodyTemperature(summaryRequest.getBodyTemperature())
                                .sleepScore(summaryRequest.getSleepScore())
                                .systolicBloodPressure(summaryRequest.getSystolicBloodPressure())
                                .diastolicBloodPressure(summaryRequest.getDiastolicBloodPressure())
                                .logDate(logDate)
                                .build();
                    });

            // 저장
            dailyHealthLogRepository.save(logEntry);

            log.info("일일 건강 데이터 저장/업데이트 성공: {}", silverId);
            return ApiResponse.success("일일 건강 데이터 저장 완료", summaryRequest.getSystolicBloodPressure());

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
