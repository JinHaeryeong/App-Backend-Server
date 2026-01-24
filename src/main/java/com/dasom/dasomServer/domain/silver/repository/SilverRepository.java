package com.dasom.dasomServer.domain.silver.repository;

import com.dasom.dasomServer.domain.silver.entity.Silver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SilverRepository extends JpaRepository<Silver, String> {
    Optional<Silver> findByLoginId(String loginId);
}
