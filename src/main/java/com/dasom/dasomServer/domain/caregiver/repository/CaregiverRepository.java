package com.dasom.dasomServer.domain.caregiver.repository;

import com.dasom.dasomServer.domain.caregiver.entity.Caregiver;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CaregiverRepository extends JpaRepository<Caregiver, Long> {
    // 로그인 ID로 찾기 (CaregiverMapper.findCaregiverByLoginId 대체)
    Optional<Caregiver> findByLoginId(String loginId);

    // 어르신 로그인 ID로 담당 지원사 찾기 (CaregiverMapper.findCaregiverBySilverLoginId 대체)
    // Caregiver 엔티티에 List<Silver> silvers 연관관계가 맺어져 있어야 작동함
    @EntityGraph(attributePaths = {"images"})
    @Query("SELECT c FROM Caregiver c JOIN c.silvers s WHERE s.loginId = :silverLoginId")
    Optional<Caregiver> findBySilverLoginId(@Param("silverLoginId") String silverLoginId);

}
