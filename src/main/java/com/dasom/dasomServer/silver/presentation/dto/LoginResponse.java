package com.dasom.dasomServer.silver.presentation.dto;

import com.dasom.dasomServer.silver.domain.Silver;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore; // 추가
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LoginResponse {
    private boolean success;
    private String message;
    private String accessToken; // Zustand 스토어용

    // 이 필드는 서비스 -> 컨트롤러 전달용으로만 쓰고, 실제 클라이언트 응답(JSON)에선 제외함
    @JsonIgnore
    private String refreshToken;

    private String loginId;
    private String name;
    private char gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime birthday;
    private String profileImageUrl;

    public static LoginResponse from(Silver silver, String accessToken, String refreshToken, String message) {
        return LoginResponse.builder()
                .success(true)
                .message(message)
                .accessToken(accessToken)
                .refreshToken(refreshToken) // 여기에 담아서 컨트롤러로 보냄
                .loginId(silver.getLoginId())
                .name(silver.getName())
                .gender(silver.getGender())
                .birthday(silver.getBirthday())
                .profileImageUrl(silver.getProfileImageUrl())
                .build();
    }
}