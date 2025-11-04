package com.dasom.dasomServer.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private Long id;
    private String silverId;
    private String refreshToken;
    // created_at 필드는 DB에서 TIMESTAMP DEFAULT CURRENT_TIMESTAMP로 처리한다고 가정
    private LocalDateTime createdAt;

    // DB 저장 시 ID와 createdAt 필드를 무시하고 silverId와 refreshToken만 사용하기 위한 생성자
    public RefreshToken(String silverId, String refreshToken) {
        this.silverId = silverId;
        this.refreshToken = refreshToken;
    }
}
