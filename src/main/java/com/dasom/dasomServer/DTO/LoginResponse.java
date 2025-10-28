package com.dasom.dasomServer.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken; // 인증 토큰 (JWT 등)
    private Long userId;
    private String username;
}