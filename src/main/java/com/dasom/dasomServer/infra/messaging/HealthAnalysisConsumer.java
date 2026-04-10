package com.dasom.dasomServer.infra.messaging;

import com.dasom.dasomServer.health.domain.HealthResultLogRepository;
import com.dasom.dasomServer.infra.ai.HealthAnalysisPipeline;
import com.dasom.dasomServer.shared.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * RabbitMQ로부터 건강 분석 요청 메시지를 수신하여 AI 파이프라인을 트리거하는 소비자
 * 메시징 인프라 레이어에 위치하며, 비즈니스 로직은 HealthAnalysisPipeline에 위임
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HealthAnalysisConsumer {

    private final HealthAnalysisPipeline healthAnalysisPipeline;
    private final HealthResultLogRepository healthResultLogRepository;

    @RabbitListener(queues = RabbitMqConfig.HEALTH_ANALYSIS_QUEUE)
    public void consume(String silverId) throws Exception { // 예외를 밖으로 던짐
        log.info("MQ 메시지 수신: silverId={}", silverId);
        try {
            if (isRecentlyAnalyzed(silverId)) {
                log.info("분석 스킵: 최근 1분 이내 분석 결과 존재 (silverId={})", silverId);
                return;
            }
            healthAnalysisPipeline.runSlidingWindowAnalysis(silverId);
            log.info("비동기 분석 완료: silverId={}", silverId);
        } catch (Exception e) {
            log.error("비동기 분석 중 오류 발생: silverId={}", silverId, e);

            throw e;
        }
    }

    private boolean isRecentlyAnalyzed(String silverId) {
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        return healthResultLogRepository.existsBySilverIdAndLogDateAfter(silverId, oneMinuteAgo);
    }
}