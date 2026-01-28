package com.dasom.dasomServer.domain.health.repository;

import com.dasom.dasomServer.domain.health.entity.HealthResultLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthResultLogRepository extends JpaRepository<HealthResultLog, Long> {
}
