package com.dasom.dasomServer.domain.health.repository;

import com.dasom.dasomServer.domain.health.entity.HealthResultLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface HealthResultLogRepository extends JpaRepository<HealthResultLog, Long> {
    // 특정 시간 이후의 로그가 있는지 확인하는 쿼리 메서드
    boolean existsBySilverIdAndLogDateAfter(String silverId, LocalDateTime dateTime);
}
