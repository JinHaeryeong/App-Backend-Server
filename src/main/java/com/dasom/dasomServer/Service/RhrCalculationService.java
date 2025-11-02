package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DAO.HealthMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Optional 임포트 필요

@Service
@Slf4j
public class RhrCalculationService {

    private final HealthMapper dataMapper;

    public RhrCalculationService(HealthMapper dataMapper) {
        this.dataMapper = dataMapper;
    }

    @Scheduled(cron = "0 0 3 * * *") // 매일 3AM 실행 (시간 조정 가능)
    public void calculateAndSaveRhr() {
        log.info("RHR 계산 스케줄러 시작: 지난 7일 데이터 분석");

        // 분석 기간 설정 (오늘부터 7일 전까지)
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        // 모든 사용자 ID를 가져옴
        List<String> silverIds = dataMapper.findAllSilverIds();

        for (String silverId : silverIds) {
            try {
                Optional<Double> minHrOptional = dataMapper.findMinHeartRateDuringDeepSleep(silverId, startDate, endDate);

                // 값이 존재하고 0보다 클 때만 업데이트 실행
                if (minHrOptional.isPresent() && minHrOptional.get() > 0) {
                    Double minHr = minHrOptional.get();

                    // silvers 테이블의 RHR 필드를 업데이트
                    dataMapper.updateRhr(silverId, minHr);
                    log.info("Silver ID {}의 RHR을 {:.2f}로 업데이트했습니다.", silverId, minHr);
                } else {
                    log.warn("Silver ID {}의 지난 7일간 유효한 RHR 데이터가 없어 업데이트를 건너뜁니다.", silverId);
                }
            } catch (Exception e) {
                log.error("Silver ID {}의 RHR 업데이트 실패.", silverId, e);
            }
        }
        log.info("RHR 계산 스케줄러 완료.");
    }
}
