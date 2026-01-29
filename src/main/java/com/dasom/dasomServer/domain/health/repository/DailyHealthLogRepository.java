package com.dasom.dasomServer.domain.health.repository;

import com.dasom.dasomServer.domain.health.entity.DailyHealthLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyHealthLogRepository extends JpaRepository<DailyHealthLog, Long> {
    Optional<DailyHealthLog> findBySilverIdAndLogDate(String silverId, LocalDate logDate);
}
