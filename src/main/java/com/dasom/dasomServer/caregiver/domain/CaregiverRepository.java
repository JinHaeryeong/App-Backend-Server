package com.dasom.dasomServer.caregiver.domain;

import java.util.Optional;

public interface CaregiverRepository {
    Optional<Caregiver> findById(Long id);
    Optional<Caregiver> findByLoginId(String loginId);
    Optional<Caregiver> findBySilverLoginId(String silverLoginId);
}
