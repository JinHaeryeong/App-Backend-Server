package com.dasom.dasomServer.guardian.infrastructure;

import com.dasom.dasomServer.guardian.domain.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GuardianJpaRepository extends JpaRepository<Guardian, Long> {
    @Query("SELECT g FROM Guardian g JOIN g.silver s WHERE s.loginId = :silverLoginId")
    List<Guardian> findBySilverLoginId(@Param("silverLoginId") String silverLoginId);
}