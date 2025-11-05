package com.dasom.dasomServer.Security;

import com.dasom.dasomServer.DAO.RefreshTokenDAO;
import com.dasom.dasomServer.DTO.RefreshToken;
import com.dasom.dasomServer.Service.UserDetailService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

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
    private final RefreshTokenDAO refreshTokenDAO;

    @Value("${jwt.secret}")
    private String secretKeyString;

    // application.yml에서 설정한 만료 시간
    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationMs;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access Token과 Refresh Token을 생성하고 Refresh Token을 DB에 저장
     */
    // [수정 1] 불필요하고 혼란을 유발하는 'Long silverId' 파라미터를 제거합니다.
    // 'loginId'만 받아서 처리합니다.
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

        // [수정 2] RefreshToken DTO 객체를 생성합니다.
        RefreshToken refreshToken = new RefreshToken();
        // [수정 3] 외래 키 제약 조건(FK -> silvers.login_id)에 따라 silverId 필드에 'loginId'(String)를 설정합니다.
        refreshToken.setSilverId(loginId);
        // [수정 4] 생성된 리프레시 토큰 값을 DTO에 설정합니다.
        refreshToken.setRefreshToken(refreshTokenValue);

        // [수정 5] 문자열이 아닌 'RefreshToken' DTO 객체를 DAO로 전달합니다.
        refreshTokenDAO.save(refreshToken);

        log.info("JWT Tokens created for {}. Access Exp: {} min", loginId, TimeUnit.MILLISECONDS.toMinutes(accessTokenExpirationMs));

        return new LoginTokenDto(accessToken, refreshTokenValue);
    }

    /**
     * 토큰에서 사용자 인증 정보를 추출
     */
    public Authentication getAuthentication(String token) {
        // 토큰에서 loginId (Subject)를 추출
        String loginId = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        // UserDetailsService를 통해 UserDetails를 로드
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginId);

        // 인증 객체 반환
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * 토큰의 유효성을 검사합니다.
     */
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

    // 토큰 생성 시 사용되는 DTO (이너 클래스로 정의)
    public static class LoginTokenDto {
        public final String accessToken;
        public final String refreshToken;
        public LoginTokenDto(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}