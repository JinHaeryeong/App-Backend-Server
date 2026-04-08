package com.dasom.dasomServer.guardian.infrastructure;

import com.dasom.dasomServer.guardian.domain.Guardian;
import com.dasom.dasomServer.guardian.domain.GuardianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GuardianRepositoryImpl implements GuardianRepository {

    private final GuardianJpaRepository guardianJpaRepository;

    @Override
    public List<Guardian> findBySilverLoginId(String silverLoginId) {
        return guardianJpaRepository.findBySilverLoginId(silverLoginId);
    }
}
