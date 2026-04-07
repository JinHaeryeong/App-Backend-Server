package com.dasom.dasomServer.caregiver.infrastructure;

import com.dasom.dasomServer.caregiver.domain.Caregiver;
import com.dasom.dasomServer.caregiver.domain.CaregiverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CaregiverRepositoryImpl implements CaregiverRepository {

    private final CaregiverJpaRepository caregiverJpaRepository;

    @Override
    public Optional<Caregiver> findById(Long id) {
        return caregiverJpaRepository.findById(id);
    }

    @Override
    public Optional<Caregiver> findByLoginId(String loginId) {
        return caregiverJpaRepository.findByLoginId(loginId);
    }

    @Override
    public Optional<Caregiver> findBySilverLoginId(String silverLoginId) {
        return caregiverJpaRepository.findBySilverLoginId(silverLoginId);
    }
}
