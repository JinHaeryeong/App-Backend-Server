package com.dasom.dasomServer.health.infrastructure;

import com.dasom.dasomServer.health.domain.DailyHealthLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyHealthLogJpaRepository extends JpaRepository<DailyHealthLog, Long> {

    Optional<DailyHealthLog> findBySilverIdAndLogDate(String silverId, LocalDate logDate);
}
