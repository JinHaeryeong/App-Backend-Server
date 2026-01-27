package com.dasom.dasomServer.domain.guardian.repository;

import com.dasom.dasomServer.domain.guardian.entity.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GuardianRepository extends JpaRepository<Guardian, Long> {
    // findGuardiansBySilverId 대체
    @Query("SELECT g FROM Guardian g JOIN g.silver s WHERE s.loginId = :silverLoginId")
    List<Guardian> findBySilverLoginId(@Param("silverLoginId") String silverLoginId);
}