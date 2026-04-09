package com.dasom.dasomServer.health.infrastructure;

import com.dasom.dasomServer.health.domain.HealthResultLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface HealthResultLogJpaRepository extends JpaRepository<HealthResultLog, Long> {

    boolean existsBySilverIdAndLogDateAfter(String silverId, LocalDateTime dateTime);
}
