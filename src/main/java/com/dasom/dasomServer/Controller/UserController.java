package com.dasom.dasomServer.Controller;

import com.dasom.dasomServer.DTO.LoginResponse;
import com.dasom.dasomServer.DTO.RegisterRequest;
import com.dasom.dasomServer.DTO.User;
import com.dasom.dasomServer.Service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
@Slf4j
public class UserController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> createUser(

            @RequestPart("user") String userJson,

            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        try {
            RegisterRequest request = objectMapper.readValue(userJson, RegisterRequest.class);
            MultipartFile profileImage = null;
            if (imageFiles != null && !imageFiles.isEmpty()) {
                // 프로필 사진은 첫 번째 파일이라고 가정하고 추출
                profileImage = imageFiles.get(0);
            }
            LoginResponse response = userService.createUser(request, (MultipartFile) profileImage);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (JsonProcessingException e) {
            log.warn("Signup failed (JSON Parse Error): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(LoginResponse.builder()
                    .success(false).message("회원가입 정보의 형식이 올바르지 않습니다.").build());
        } catch (IllegalStateException e) {
            // ID 중복 (409 CONFLICT)
            log.warn("Signup failed (IllegalState): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(LoginResponse.builder()
                    .success(false).message(e.getMessage()).build());
        } catch (RuntimeException e) {
            // 파일 저장 실패 등 (500 INTERNAL_SERVER_ERROR)
            log.error("Signup failed (File/Runtime Error): {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LoginResponse.builder()
                    .success(false).message("회원가입 중 서버 오류가 발생했습니다.").build());
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {

        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody User loginInfo) {
        log.info("Login Info: {} ", loginInfo);

        try {
            LoginResponse response = userService.authenticateUser(
                    loginInfo.getLoginId(),
                    loginInfo.getPassword()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 인증 실패 (401 UNAUTHORIZED)
            log.warn("Login failed (Auth): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(LoginResponse.builder()
                    .success(false).message(e.getMessage()).build());
        }
    }
}