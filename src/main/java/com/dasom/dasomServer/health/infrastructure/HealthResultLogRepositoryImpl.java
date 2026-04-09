package com.dasom.dasomServer.health.infrastructure;

import com.dasom.dasomServer.health.domain.HealthResultLog;
import com.dasom.dasomServer.health.domain.HealthResultLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class HealthResultLogRepositoryImpl implements HealthResultLogRepository {

    private final HealthResultLogJpaRepository jpaRepository;

    @Override
    public HealthResultLog save(HealthResultLog healthResultLog) {
        return jpaRepository.save(healthResultLog);
    }

    @Override
    public boolean existsBySilverIdAndLogDateAfter(String silverId, LocalDateTime dateTime) {
        return jpaRepository.existsBySilverIdAndLogDateAfter(silverId, dateTime);
    }
}
