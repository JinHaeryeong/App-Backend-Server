package com.dasom.dasomServer.global.security;

import com.dasom.dasomServer.domain.silver.entity.RefreshToken; // 엔티티 임포트
import com.dasom.dasomServer.domain.silver.repository.RefreshTokenRepository; // 리포지토리 임포트
import com.dasom.dasomServer.domain.silver.service.UserDetailService;
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

    @Lazy
    private final UserDetailService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository; // Mapper 대신 Repository 주입

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationMs;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token과 Refresh Token을 생성하고 Refresh Token을 DB에 저장/갱신
     */
    @Transactional // DB 상태가 변하므로 트랜잭션 처리 필수!
    public LoginTokenDto createToken(String loginId) {

        String accessToken = Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        String refreshTokenValue = Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        // [핵심] JPA를 이용한 UPSERT 로직 (MyBatis의 ON DUPLICATE KEY UPDATE 대체)
        refreshTokenRepository.findBySilverId(loginId)
                .ifPresentOrElse(
                        existingToken -> {
                            log.info("기존 리프레시 토큰 갱신: {}", loginId);
                            existingToken.updateToken(refreshTokenValue); // Dirty Checking으로 자동 업데이트
                        },
                        () -> {
                            log.info("새 리프레시 토큰 저장: {}", loginId);
                            refreshTokenRepository.save(RefreshToken.builder()
                                    .silverId(loginId)
                                    .refreshToken(refreshTokenValue)
                                    .build());
                        }
                );

        log.info("JWT Tokens created for {}. Access Exp: {} min", loginId, TimeUnit.MILLISECONDS.toMinutes(accessTokenExpirationMs));

        return new LoginTokenDto(accessToken, refreshTokenValue);
    }

    /** 토큰에서 사용자 인증 정보를 추출 */
    public Authentication getAuthentication(String token) {
        String loginId = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /** 토큰의 유효성을 검사 */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public static class LoginTokenDto {
        public final String accessToken;
        public final String refreshToken;
        public LoginTokenDto(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}