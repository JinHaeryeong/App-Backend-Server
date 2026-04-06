package com.dasom.dasomServer.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Slf4j
@Configuration
public class RabbitMqConfig {

    public static final String HEALTH_ANALYSIS_QUEUE = "health.analysis.queue";

    @Bean
    public Queue healthAnalysisQueue() {
        // 이름이 "health.analysis.queue"인 큐를 생성
        return new Queue(HEALTH_ANALYSIS_QUEUE, true);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        // 재시도 템플릿 설정 (2단계 조치)
        RetryTemplate retryTemplate = new RetryTemplate();

        // 최대 3번까지 재시도
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 실패 시 1초부터 시작해서 2배씩 간격을 늘려가며 시도 (1s -> 2s -> 4s)
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000L);
        backOffPolicy.setMultiplier(2.0);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        rabbitTemplate.setRetryTemplate(retryTemplate);

        // 발행 확인 콜백 설정 (1단계 조치)
        // yml의 publisher-confirm-type: correlated 설정과 짝꿍
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("메시지가 RabbitMQ에 안전하게 도착했습니다.");
            } else {
                log.error("메시지 발행 최종 실패! 사유: {}. 이 데이터는 별도 처리가 필요합니다.", cause);
            }
        });

        return rabbitTemplate;
    }
}
