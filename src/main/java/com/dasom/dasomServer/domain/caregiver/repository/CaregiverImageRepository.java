package com.dasom.dasomServer.domain.caregiver.repository;

import com.dasom.dasomServer.domain.caregiver.entity.CaregiverImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaregiverImageRepository extends JpaRepository<CaregiverImage, Long> {

    /**
     * 지원사 ID로 조회해서 가장 먼저 등록된 이미지 1장만 가져오기
     * findFirstBy + [연관관계필드명] + Id + OrderBy + [정렬기준]
     */
    Optional<CaregiverImage> findFirstByCaregiverIdOrderByIdAsc(Long caregiverId);
}