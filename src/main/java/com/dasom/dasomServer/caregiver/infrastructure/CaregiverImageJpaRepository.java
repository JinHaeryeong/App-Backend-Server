package com.dasom.dasomServer.caregiver.infrastructure;

import com.dasom.dasomServer.caregiver.domain.CaregiverImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaregiverImageJpaRepository extends JpaRepository<CaregiverImage, Long> {

    Optional<CaregiverImage> findFirstByCaregiverIdOrderByIdAsc(Long caregiverId);
}
