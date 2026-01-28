package com.dasom.dasomServer.domain.health.repository;

import com.dasom.dasomServer.domain.health.entity.HealthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {
    List<HealthLog> findTop6BySilverIdOrderByLogDateDesc(String silverId);

    int countBySilverId(String silverId);
}
