package com.dasom.dasomServer.domain.silver.repository;

import com.dasom.dasomServer.domain.silver.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findBySilverId(String silverId);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Modifying
    @Transactional
    void deleteBySilverId(String silverId);
}