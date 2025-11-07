package com.dasom.dasomServer.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy", timezone = "Asia/Seoul")
    private Date birthday;
    private List<String> images;
}