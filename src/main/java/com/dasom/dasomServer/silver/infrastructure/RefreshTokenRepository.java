package com.dasom.dasomServer.silver.infrastructure;

import com.dasom.dasomServer.silver.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByLoginId(String loginId);

    @Modifying
    @Transactional
    void deleteByLoginId(String loginId);
}