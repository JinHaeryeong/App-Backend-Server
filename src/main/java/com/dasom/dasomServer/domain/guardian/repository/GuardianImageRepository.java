package com.dasom.dasomServer.domain.guardian.repository;

import com.dasom.dasomServer.domain.guardian.entity.GuardianImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GuardianImageRepository extends JpaRepository<GuardianImage, Long> {

    // 쿼리문 없이 이름
    // "보호자 ID로 검색해서 storedFileName 목록만 가져와라"는 뜻~
    Optional<GuardianImage> findFirstByGuardianIdOrderByIdAsc(Long guardianId);
}