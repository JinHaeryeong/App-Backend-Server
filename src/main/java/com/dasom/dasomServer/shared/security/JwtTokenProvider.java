package com.dasom.dasomServer.shared.security;

import com.dasom.dasomServer.silver.domain.RefreshToken; // 엔티티 임포트
import com.dasom.dasomServer.silver.infrastructure.RefreshTokenRepository; // 리포지토리 임포트
import com.dasom.dasomServer.silver.application.UserDetailService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional; // @Transactional 추가

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final UserDetailService userDetailsService;

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationMs;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public TokenDto createToken(String loginId, String role) {
        Date now = new Date();

        String accessToken = Jwts.builder()
                .subject(loginId)
                .claim("role", role) // Role 추가!
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();

        String refreshToken = Jwts.builder()
                .subject(loginId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();

        return new TokenDto(accessToken, refreshToken);
    }

    public Authentication getAuthentication(String token) {
        String loginId = getLoginId(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
        // Authorities도 userDetails에서 꺼내서 넣어줌
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getLoginId(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()).build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("JWT 검증 실패: {}", e.getMessage());
            return false;
        }
    }
}