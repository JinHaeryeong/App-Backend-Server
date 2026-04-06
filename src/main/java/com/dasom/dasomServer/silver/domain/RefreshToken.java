package com.dasom.dasomServer.silver.domain;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "refresh_tokens") // 테이블 이름 명시
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String token;

    public RefreshToken(String loginId, String token) {
        this.loginId = loginId;
        this.token = token;
    }

    public void updateToken(String newToken) {
        this.token = newToken;
    }
}
