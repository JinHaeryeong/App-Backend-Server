package com.dasom.dasomServer.health.infrastructure;

import com.dasom.dasomServer.health.domain.HealthLog;
import com.dasom.dasomServer.health.domain.HealthLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HealthLogRepositoryImpl implements HealthLogRepository {

    private final HealthLogJpaRepository jpaRepository;

    @Override
    public HealthLog save(HealthLog healthLog) {
        return jpaRepository.save(healthLog);
    }

    @Override
    public List<HealthLog> findTop6BySilverIdOrderByLogDateDesc(String silverId) {
        return jpaRepository.findTop6BySilverIdOrderByLogDateDesc(silverId);
    }

    @Override
    public int countBySilverId(String silverId) {
        return jpaRepository.countBySilverId(silverId);
    }

    @Override
    public Optional<Double> findMinHeartRateDuringDeepSleep(String silverId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findMinHeartRateDuringDeepSleep(silverId, startDate, endDate);
    }
}
