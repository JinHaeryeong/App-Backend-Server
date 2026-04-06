package com.dasom.dasomServer.silver.presentation;

import com.dasom.dasomServer.silver.application.UserService;
import com.dasom.dasomServer.silver.presentation.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class SilverController {

    private final UserService userService;

    @Value("${jwt.cookie-secure}")
    private boolean cookieSecure;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SignupResponse> createUser(
            @RequestPart("user") @Valid SignupRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        userService.signupUser(request, imageFile);
        return ResponseEntity.ok(SignupResponse.success());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @RequestBody @Valid LoginRequest loginInfo,
            HttpServletResponse response) {

        LoginResponse loginResponse = userService.authenticateUser(loginInfo.getLoginId(), loginInfo.getPassword());

        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(refreshTokenExpiration / 1000)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.logout(loginId);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok("로그아웃 성공");
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<SilverResponse> getUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        SilverResponse response = userService.getUser(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<SilverResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}