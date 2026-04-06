package com.dasom.dasomServer.silver.presentation.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupResponse {
    private final boolean success;
    private final String message;


    @Builder
    private SignupResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static SignupResponse success() {
        return SignupResponse.builder()
                .success(true)
                .message("회원가입이 완료되었습니다.")
                .build();
    }
}