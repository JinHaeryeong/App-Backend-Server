package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DTO.LoginRequest;
import com.dasom.dasomServer.DTO.LoginResponse;

public interface AuthService {
    LoginResponse authenticate(LoginRequest request);
}