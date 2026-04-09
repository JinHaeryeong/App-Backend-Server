package com.dasom.dasomServer.health.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HealthLogRepository {
    HealthLog save(HealthLog healthLog);
    List<HealthLog> findTop6BySilverIdOrderByLogDateDesc(String silverId);
    int countBySilverId(String silverId);
    Optional<Double> findMinHeartRateDuringDeepSleep(String silverId, LocalDateTime startDate, LocalDateTime endDate);
}
