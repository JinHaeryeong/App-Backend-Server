package com.dasom.dasomServer.health.application;

import com.dasom.dasomServer.health.domain.HealthLogRepository;
import com.dasom.dasomServer.silver.domain.Silver;
import com.dasom.dasomServer.silver.infrastructure.SilverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RhrCalculationService {

    private final HealthLogRepository healthLogRepository;
    private final SilverRepository silverRepository;

    /**
     * 매일 새벽 3시: 지난 7일간의 딥슬립 데이터를 기반으로 어르신별 RHR 갱신
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void calculateAndSaveRhr() {
        log.info("RHR 계산 스케줄러 시작: 지난 7일 데이터 분석");

        LocalDateTime endDate   = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);

        List<Silver> silvers = silverRepository.findAll();

        for (Silver silver : silvers) {
            String silverId = silver.getLoginId();
            try {
                healthLogRepository
                        .findMinHeartRateDuringDeepSleep(silverId, startDate, endDate)
                        .filter(minHr -> minHr > 0)
                        .ifPresentOrElse(
                                minHr -> {
                                    silver.updateRhr(minHr);
                                    log.info("RHR 업데이트: silverId={}, rhr={}", silverId, minHr);
                                },
                                () -> log.warn("유효한 RHR 데이터 없음, 업데이트 스킵: silverId={}", silverId)
                        );
            } catch (Exception e) {
                log.error("RHR 업데이트 실패: silverId={}", silverId, e);
            }
        }

        log.info("RHR 계산 스케줄러 완료");
    }
}
