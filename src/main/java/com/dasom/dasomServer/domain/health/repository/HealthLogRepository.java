package com.dasom.dasomServer.domain.health.repository;

import com.dasom.dasomServer.domain.health.entity.HealthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {
    List<HealthLog> findTop6BySilverIdOrderByLogDateDesc(String silverId);

    int countBySilverId(String silverId);

    // RHR 계산을 위한 딥슬립 중 최저 심박수 조회
    @Query("SELECT MIN(h.heartRate) FROM HealthLog h " +
            "WHERE h.silverId = :silverId " +
            "AND h.logDate BETWEEN :startDate AND :endDate " +
            "AND h.deepMin > 0") // 딥슬립 기록이 있는 경우
    Optional<Double> findMinHeartRateDuringDeepSleep(
            @Param("silverId") String silverId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
