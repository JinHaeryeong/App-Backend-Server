package com.dasom.dasomServer.infra.ai;

import ai.onnxruntime.OrtException;
import com.dasom.dasomServer.health.domain.HealthLog;
import com.dasom.dasomServer.health.domain.HealthLogRepository;
import com.dasom.dasomServer.health.domain.HealthResultLog;
import com.dasom.dasomServer.health.domain.HealthResultLogRepository;
import com.dasom.dasomServer.health.domain.HealthStatus;
import com.dasom.dasomServer.health.presentation.dto.HealthDataRequest;
import com.dasom.dasomServer.silver.domain.Silver;
import com.dasom.dasomServer.silver.infrastructure.SilverRepository;
import com.dasom.dasomServer.shared.common.ApiResponse;
import com.dasom.dasomServer.shared.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 건강 데이터 수집부터 AI 추론 결과 저장까지의 전체 파이프라인을 조율하는 서비스.
 * - 데이터 저장: HealthLogRepository
 * - AI 추론:     LstmInferenceEngine
 * - 이벤트 발행: RabbitTemplate
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAnalysisPipeline {

    private static final int    N_STEPS     = 6;
    private static final double DEFAULT_RHR = 70.0;

    private final HealthLogRepository healthLogRepository;
    private final HealthResultLogRepository resultRepository;
    private final SilverRepository silverRepository;
    private final LstmInferenceEngine inferenceEngine;
    private final LstmModelSession modelSession;
    private final RabbitTemplate rabbitTemplate;

    /**
     * 생체 신호를 저장하고 AI 분석 이벤트를 발행한다.
     * 데이터가 N_STEPS 미만이면 축적 중 메시지를 반환한다.
     */
    @Transactional
    public ApiResponse<?> collectAndPublish(HealthDataRequest request) {
        String silverId = request.getSilverId();
        try {
            HealthLog newLog = HealthLog.from(
                    silverId,
                    resolveHeartRate(silverId, request.getHeartRateAvg()),
                    request.getWalkingSteps(),
                    request.getTotalCaloriesBurned(),
                    request.getSpo2(),
                    resolveLogDate(request.getLogDate()),
                    request.getSleepDurationMin(),
                    request.getSleepStageDeepMin(),
                    request.getSleepStageLightMin(),
                    request.getSleepStageRemMin(),
                    request.getSleepStageWakeMin()
            );

            fillMissingDataPointsIfNeeded(silverId, newLog.getLogDate());
            healthLogRepository.save(newLog);

            long count = healthLogRepository.countBySilverId(silverId);
            if (count < N_STEPS) {
                return ApiResponse.success(String.format("데이터 축적 중 (%d/%d)", count, N_STEPS));
            }

            publishAfterCommit(silverId);
            return ApiResponse.success("데이터 수집 완료. 분석이 백그라운드에서 시작됩니다");

        } catch (Exception e) {
            log.error("데이터 처리 실패: silverId={}", silverId, e);
            return ApiResponse.error("처리 중 오류 발생", e.getClass().getSimpleName());
        }
    }

    /**
     * 슬라이딩 윈도우 분석을 실행하고 결과를 저장한다.
     * HealthAnalysisConsumer(MQ 소비자)에서 호출된다.
     */
    @Transactional
    public void runSlidingWindowAnalysis(String silverId) throws OrtException {
        if (!modelSession.isReady()) {
            log.error("AI 분석 실패: ONNX 세션 미초기화 - silverId={}", silverId);
            return;
        }

        List<HealthLog> sequence = healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId);
        if (sequence.size() < N_STEPS) {
            log.warn("AI 분석 스킵: 데이터 부족 ({}/{}), silverId={}", sequence.size(), N_STEPS, silverId);
            return;
        }
        Collections.reverse(sequence); // 오름차순 정렬

        Silver silver = silverRepository.findByLoginId(silverId)
                .orElseThrow(() -> new RuntimeException("어르신을 찾을 수 없습니다: " + silverId));

        HealthStatus status = inferenceEngine.infer(sequence, silver);
        log.info("AI 추론 완료: silverId={}, status={}", silverId, status);

        resultRepository.save(HealthResultLog.builder()
                .silverId(silverId)
                .label(status)
                .logDate(LocalDateTime.now())
                .build());
    }

    private LocalDateTime resolveLogDate(LocalDateTime requestedDate) {
        return requestedDate != null ? requestedDate : LocalDateTime.now();
    }

    private int resolveHeartRate(String silverId, Long heartRateAvg) {
        if (heartRateAvg != null) return heartRateAvg.intValue();
        log.warn("심박수 데이터 누락 - 기본값 적용: silverId={}, default={}", silverId, (int) DEFAULT_RHR);
        return (int) DEFAULT_RHR;
    }

    private void fillMissingDataPointsIfNeeded(String silverId, LocalDateTime newRecordTime) {
        healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId)
                .stream()
                .findFirst()
                .ifPresent(lastLog -> fillGap(lastLog, newRecordTime));
    }

    private void fillGap(HealthLog lastLog, LocalDateTime newRecordTime) {
        if (lastLog.getLogDate() == null) return;

        LocalDateTime fillTime = lastLog.getLogDate().plusMinutes(10);
        while (fillTime.isBefore(newRecordTime.minusSeconds(1))) {
            healthLogRepository.save(HealthLog.filledFrom(lastLog, fillTime));
            fillTime = fillTime.plusMinutes(10);
        }
    }

    private void publishAfterCommit(String silverId) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    rabbitTemplate.convertAndSend(RabbitMqConfig.HEALTH_ANALYSIS_QUEUE, silverId);
                }
            });
        } else {
            rabbitTemplate.convertAndSend(RabbitMqConfig.HEALTH_ANALYSIS_QUEUE, silverId);
        }
    }
}
