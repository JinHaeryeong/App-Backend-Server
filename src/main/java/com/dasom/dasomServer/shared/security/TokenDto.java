package com.dasom.dasomServer.shared.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TokenDto {
    private String accessToken;
    private String refreshToken;
}