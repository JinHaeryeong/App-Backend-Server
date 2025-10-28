package com.dasom.dasomServer.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String loginId;
    private String password;
}