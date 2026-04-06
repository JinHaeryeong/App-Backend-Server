package com.dasom.dasomServer.silver.infrastructure;

import com.dasom.dasomServer.silver.domain.Silver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


public interface SilverRepository extends JpaRepository<Silver, Long> { // PK 타입 Long 확인!

    boolean existsByLoginId(String loginId);

    Optional<Silver> findByLoginId(String loginId);

    @Override
    @NonNull
    @Query("SELECT DISTINCT s FROM Silver s")
    List<Silver> findAll();
}