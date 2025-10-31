package com.dasom.dasomServer.Controller;

import com.dasom.dasomServer.DTO.LoginRequest;
import com.dasom.dasomServer.DTO.LoginResponse;
import com.dasom.dasomServer.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // 새로운 인증 전용 경로
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class AuthController {

    private final AuthService authService;

    // 로그인 (POST /api/auth/login)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        LoginResponse response = authService.authenticate(request);

        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build();
    }
}