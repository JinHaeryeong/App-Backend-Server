package com.dasom.dasomServer.domain.silver.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // bigint, PRI, auto_increment

    @Column(name = "silver_id", nullable = false, unique = true, length = 50)
    private String silverId; // varchar(50), UNI

    @Column(name = "refresh_token", length = 512)
    private String refreshToken; // varchar(512)

    @CreationTimestamp // DB의 CURRENT_TIMESTAMP 설정을 자바에서 처리
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // timestamp

    // 토큰 갱신용 메서드 (업데이트 시 사용)
    public void updateToken(String newToken) {
        this.refreshToken = newToken;
    }
}
