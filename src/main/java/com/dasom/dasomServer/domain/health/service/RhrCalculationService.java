package com.dasom.dasomServer.domain.health.service;

import com.dasom.dasomServer.domain.health.repository.HealthLogRepository;
import com.dasom.dasomServer.silver.domain.Silver;
import com.dasom.dasomServer.silver.infrastructure.SilverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Optional 임포트 필요

@Service
@Slf4j
@RequiredArgsConstructor
public class RhrCalculationService {

    private final HealthLogRepository healthLogRepository;
    private final SilverRepository silverRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void calculateAndSaveRhr() {
        // 시작 알림 및 기간 명시
        log.info("RHR 계산 스케줄러 시작: 지난 7일 데이터 분석");

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        // 모든 어르신 목록 조회
        List<Silver> silvers = silverRepository.findAll();

        for (Silver silver : silvers) {
            String silverId = silver.getLoginId();
            try {
                // 최저 심박수 조회
                Optional<Double> minHrOptional = healthLogRepository.findMinHeartRateDuringDeepSleep(silverId, startDate, endDate);

                // 성공 시 포맷팅된 출력
                if (minHrOptional.isPresent() && minHrOptional.get() > 0) {
                    Double minHr = minHrOptional.get();
                    silver.updateRhr(minHr); // JPA 더티 체킹 활용
                    log.info("Silver ID {}의 RHR을 {}로 업데이트", silverId, minHr);
                }
                // 데이터 부족 시 경고
                else {
                    log.warn("Silver ID {}의 지난 7일간 유효한 RHR 데이터가 없어 업데이트를 건너뜁니다", silverId);
                }
            } catch (Exception e) {
                // 에러 발생 시 추적 가능하게 로깅
                log.error("Silver ID {}의 RHR 업데이트 실패", silverId, e);
            }
        }

        // 완료 알림
        log.info("RHR 계산 스케줄러 완료");
    }
}
