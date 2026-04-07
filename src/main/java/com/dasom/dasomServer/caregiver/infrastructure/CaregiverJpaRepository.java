package com.dasom.dasomServer.caregiver.infrastructure;

import com.dasom.dasomServer.caregiver.domain.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CaregiverJpaRepository extends JpaRepository<Caregiver, Long> {

    Optional<Caregiver> findByLoginId(String loginId);

    @Query("SELECT c FROM Caregiver c JOIN c.silvers s WHERE s.loginId = :silverLoginId")
    Optional<Caregiver> findBySilverLoginId(@Param("silverLoginId") String silverLoginId);
}
