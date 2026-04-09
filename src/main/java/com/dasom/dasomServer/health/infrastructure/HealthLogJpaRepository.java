package com.dasom.dasomServer.health.infrastructure;

import com.dasom.dasomServer.health.domain.HealthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HealthLogJpaRepository extends JpaRepository<HealthLog, Long> {

    List<HealthLog> findTop6BySilverIdOrderByLogDateDesc(String silverId);

    int countBySilverId(String silverId);

    @Query("SELECT MIN(h.heartRate) FROM HealthLog h " +
           "WHERE h.silverId = :silverId " +
           "AND h.logDate BETWEEN :startDate AND :endDate " +
           "AND h.deepMin > 0")
    Optional<Double> findMinHeartRateDuringDeepSleep(
            @Param("silverId") String silverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
