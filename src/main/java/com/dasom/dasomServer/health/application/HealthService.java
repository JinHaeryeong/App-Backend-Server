package com.dasom.dasomServer.health.application;

import com.dasom.dasomServer.health.domain.DailyHealthLog;
import com.dasom.dasomServer.health.domain.DailyHealthLogRepository;
import com.dasom.dasomServer.health.domain.HealthLog;
import com.dasom.dasomServer.health.domain.HealthLogRepository;
import com.dasom.dasomServer.health.presentation.dto.DailyHealthLogRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthService {

    private final HealthLogRepository healthLogRepository;
    private final DailyHealthLogRepository dailyHealthLogRepository;

    /**
     * 일일 건강 로그 upsert
     * - 당일 기록이 있으면 도메인 update()로 갱신 (dirty checking)
     * - 없으면 정적 팩토리로 신규 생성
     */
    @Transactional
    public DailyHealthLog upsertDailyHealthLog(DailyHealthLogRequest request) {
        DailyHealthLog result = dailyHealthLogRepository
                .findBySilverIdAndLogDate(request.getSilverId(), request.getLogDate())
                .map(existing -> {
                    existing.update(
                            request.getWeight(),
                            request.getBloodSugar(),
                            request.getBodyTemperature(),
                            request.getSleepScore(),
                            request.getSystolicBloodPressure(),
                            request.getDiastolicBloodPressure()
                    );
                    return existing;
                })
                .orElseGet(() -> dailyHealthLogRepository.save(
                        DailyHealthLog.from(
                                request.getSilverId(),
                                request.getWeight(),
                                request.getBloodSugar(),
                                request.getBodyTemperature(),
                                request.getSleepScore(),
                                request.getSystolicBloodPressure(),
                                request.getDiastolicBloodPressure(),
                                request.getLogDate()
                        )
                ));

        log.info("일일 건강 데이터 저장/업데이트 완료: silverId={}", request.getSilverId());
        return result;
    }

    /**
     * 최근 건강 로그 6개 조회
     */
    @Transactional(readOnly = true)
    public List<HealthLog> findRecentLogs(String silverId) {
        return healthLogRepository.findTop6BySilverIdOrderByLogDateDesc(silverId);
    }
}
