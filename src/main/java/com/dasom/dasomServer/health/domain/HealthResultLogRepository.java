package com.dasom.dasomServer.health.domain;

import java.time.LocalDateTime;

public interface HealthResultLogRepository {
    HealthResultLog save(HealthResultLog healthResultLog);
    boolean existsBySilverIdAndLogDateAfter(String silverId, LocalDateTime dateTime);
}
