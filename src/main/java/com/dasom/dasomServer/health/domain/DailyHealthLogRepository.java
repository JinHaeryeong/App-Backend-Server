package com.dasom.dasomServer.health.domain;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyHealthLogRepository {
    DailyHealthLog save(DailyHealthLog dailyHealthLog);
    Optional<DailyHealthLog> findBySilverIdAndLogDate(String silverId, LocalDate logDate);
}
