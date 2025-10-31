// com.dasom.dasomServer.Service.AuthService.java
package com.dasom.dasomServer.service;

import com.dasom.dasomServer.dto.LoginRequest;
import com.dasom.dasomServer.dto.LoginResponse;

public interface AuthService {
    LoginResponse authenticate(LoginRequest request);
}