package com.dasom.dasomServer.domain.silver.repository;

import com.dasom.dasomServer.domain.silver.entity.Silver;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface SilverRepository extends JpaRepository<Silver, Long> { // PK 타입 Long 확인!

    /** loginId로 회원 존재 여부 확인 (existsByLoginId 대체) */
    boolean existsByLoginId(String loginId);

    /** loginId로 회원 단건 조회 (findByLoginId 대체)
     * @EntityGraph는 이미지를 한 번에 긁어와서 성능을 높임 (N+1 문제 방지)
     */
    @EntityGraph(attributePaths = {"images"})
    Optional<Silver> findByLoginId(String loginId);

    // 전체 조회 시에도 N+1 문제를 방지하기 위해 findAll 재정의
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"images"})
    @Query("SELECT DISTINCT s FROM Silver s")
    List<Silver> findAll();

    @Modifying
    @Transactional
    void deleteByLoginId(String loginId);

}