package com.dasom.dasomServer.domain.silver.controller;

import com.dasom.dasomServer.domain.silver.dto.LoginRequest;
import com.dasom.dasomServer.domain.silver.dto.LoginResponse;
import com.dasom.dasomServer.domain.silver.dto.RegisterRequest;
import com.dasom.dasomServer.domain.silver.dto.SilverResponse;
import com.dasom.dasomServer.domain.silver.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
@Slf4j
public class SilverController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    // 헬퍼 메서드
    private boolean isInvalidPath(String input) {
        return input != null && (input.contains("..") || input.contains("/") || input.contains("\\"));
    }

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> createUser(
            @RequestPart("user") String userJson,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        try {
            RegisterRequest request = objectMapper.readValue(userJson, RegisterRequest.class);

            if (isInvalidPath(request.getLoginId())) {
                throw new IllegalArgumentException("아이디에 유효하지 않은 문자가 포함되어 있습니다.");
            }
            LoginResponse response = userService.createUser(request, imageFiles);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (JsonProcessingException e) {
            log.warn("회원 가입 실패 (JSON Parse Error): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginResponse.builder()
                    .success(false).message("회원가입 정보의 형식이 올바르지 않습니다.").build());
        } catch (IllegalStateException e) {
            log.warn("회원 가입 실패 (IllegalState): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(LoginResponse.builder()
                    .success(false).message(e.getMessage()).build());
        } catch (RuntimeException e) {
            log.error("회원 가입 실패 (File/Runtime Error): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LoginResponse.builder()
                    .success(false).message("회원가입 중 서버 오류가 발생했습니다.").build());
        }
    }

    @GetMapping("/users/{id}")
    // ResponseEntity<User> -> ResponseEntity<SilverResponse>로 타입 변경
    public ResponseEntity<SilverResponse> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/users")
    // List<User> -> List<SilverResponse>로 타입 변경
    public ResponseEntity<List<SilverResponse>> getAllUsers() {
        List<SilverResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/login")
    // @RequestBody 타입을 User DTO 대신 RegisterRequest
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginInfo) {
        log.info("로그인 시도 아이디 ID: {} ", loginInfo.getLoginId());

        try {
            LoginResponse response = userService.authenticateUser(
                    loginInfo.getLoginId(),
                    loginInfo.getPassword()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("로그인 실패 (Auth): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse.builder()
                    .success(false).message(e.getMessage()).build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() { // SecurityContext에서 가져오는 게 정석
        String loginId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("로그아웃 ID: {}", loginId);
        userService.logout(loginId);
        return ResponseEntity.ok().body("로그아웃 성공");
    }
}