package com.dasom.dasomServer.infra.ai;

import com.dasom.dasomServer.global.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LstmAnalysisConsumer {
    private final LstmInferenceService lstmInferenceService;

    @RabbitListener(queues = RabbitMqConfig.HEALTH_ANALYSIS_QUEUE)
    public void receiveMessage(String silverId) {
        log.info("MQ 메시지 수신: 어르신 ID = {}", silverId);
        try {
            // 이제 여기서 진짜 무거운 분석을 실행
            // (이 로직은 별도의 스레드에서 돌기 때문에 API 응답에는 영향 0!)
            lstmInferenceService.triggerSlidingWindowAnalysis(silverId);
            log.info("비동기 분석 완료: {}", silverId);
        } catch (Exception e) {
            log.error("비동기 분석 중 에러 발생: {}", e.getMessage());
        }
    }
}
