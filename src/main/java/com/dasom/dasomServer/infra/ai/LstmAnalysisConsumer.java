package com.dasom.dasomServer.infra.ai;

import com.dasom.dasomServer.health.domain.HealthResultLogRepository;
import com.dasom.dasomServer.shared.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class LstmAnalysisConsumer {
    private final LstmInferenceService lstmInferenceService;
    private final HealthResultLogRepository healthResultLogRepository;

    @RabbitListener(queues = RabbitMqConfig.HEALTH_ANALYSIS_QUEUE)
    public void receiveMessage(String silverId) {
        log.info("MQ 메시지 수신: 어르신 ID = {}", silverId);
        try {
            LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
            boolean alreadyProcessed = healthResultLogRepository.existsBySilverIdAndLogDateAfter(silverId, oneMinuteAgo);

            if (alreadyProcessed) {
                log.info("비동기 분석 스킵: 최근 1분 이내에 이미 분석된 결과가 존재함 (SilverId: {})", silverId);
                return; // 이미 처리되었으므로 로직 종료
            }
            // 여기서 무거운 분석을 실행
            // 이 로직은 별도의 스레드에서 돌기 때문에 API 응답에는 영향 X
            lstmInferenceService.triggerSlidingWindowAnalysis(silverId);
            log.info("비동기 분석 완료: {}", silverId);
        } catch (Exception e) {
            log.error("비동기 분석 중 에러 발생: {}", e.getMessage());
        }
    }
}
