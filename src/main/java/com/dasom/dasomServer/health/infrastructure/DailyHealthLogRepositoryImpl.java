package com.dasom.dasomServer.health.infrastructure;

import com.dasom.dasomServer.health.domain.DailyHealthLog;
import com.dasom.dasomServer.health.domain.DailyHealthLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DailyHealthLogRepositoryImpl implements DailyHealthLogRepository {

    private final DailyHealthLogJpaRepository jpaRepository;

    @Override
    public DailyHealthLog save(DailyHealthLog dailyHealthLog) {
        return jpaRepository.save(dailyHealthLog);
    }

    @Override
    public Optional<DailyHealthLog> findBySilverIdAndLogDate(String silverId, LocalDate logDate) {
        return jpaRepository.findBySilverIdAndLogDate(silverId, logDate);
    }
}
