package com.dasom.dasomServer.domain.health.service;

import com.dasom.dasomServer.DTO.HealthRequest;
import com.dasom.dasomServer.domain.health.entity.HealthLog;
import com.dasom.dasomServer.domain.health.mapper.HealthMapper;
import com.dasom.dasomServer.domain.health.repository.HealthLogRepository;
import com.dasom.dasomServer.global.common.ApiResponse;
import com.dasom.dasomServer.DTO.DailyHealthLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthService {

    private final HealthMapper healthMapper;
    private final HealthLogRepository healthLogRepository;

    @Transactional
    public ApiResponse<?> upsertDailyHealthLog(DailyHealthLogRequest summaryRequest) {
        String silverId = summaryRequest.getSilverId();

        try {
            // 데이터베이스 저장
            // Mapper를 통해 BloodPressureSummaryRequest 내용을 DB에 삽입
            int rowsAffected = healthMapper.upsertDailyHealthLog(summaryRequest);

            if (rowsAffected > 0) {
                log.info("일일 혈압 측정 데이터 저장 성공: {}", silverId);
                // 성공 응답 시 최종 수축기 혈압 값을 반환 (대시보드 업데이트 등에 사용 가능)
                return ApiResponse.success("일일 혈압 최종 측정 데이터 저장 완료", summaryRequest.getSystolicBloodPressure());
            } else {
                log.warn("일일 혈압 측정 데이터 저장 실패: {}", silverId);
                return ApiResponse.error("DB에 혈압 측정 정보를 저장하지 못했습니다.", "DB_ERROR");
            }

        } catch (Exception e) {
            log.error("일일 혈압 측정 데이터 처리 중 오류 발생: {}", silverId, e);
            return ApiResponse.error("일일 혈압 측정 데이터 처리 중 서버 오류 발생: " + e.getMessage(), "SERVER_ERROR");
        }
    }

    // HealthService.java

    // JPA 테스트용~
    public List<HealthLog> findRecentLogs(String silverId) {

        return healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId);
    }

    // 테스트할때 쓴거
    public List<HealthRequest> findSequenceData(String silverId, LocalDateTime startTime, LocalDateTime endTime, int nSteps) {
        // MyBatis Mapper를 호출하여 시퀀스 데이터를 가져옴
        return healthMapper.findSequenceData(silverId, startTime, endTime, nSteps);
    }
}
