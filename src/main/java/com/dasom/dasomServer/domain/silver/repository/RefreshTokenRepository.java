package com.dasom.dasomServer.domain.silver.repository;

import com.dasom.dasomServer.domain.silver.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findBySilverId(String silverId);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    void deleteBySilverId(String silverId);
}