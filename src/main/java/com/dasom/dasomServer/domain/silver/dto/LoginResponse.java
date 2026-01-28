package com.dasom.dasomServer.domain.silver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class LoginResponse {
    private boolean success;
    private String message;

    private String accessToken; // 인증 토큰 (JWT 등)
    private String loginId;
    private String name;
    private char gender;

    // Date 대신 LocalDateTime으로 변경!
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime birthday;
    private List<String> images;
}