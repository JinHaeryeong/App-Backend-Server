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
    private final ObjectMapper objectMapper; // 💡 3. (@RequiredArgsConstructor를 통해) JSON 변환기 주입

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> createUser(

            // 💡 4. [핵심 수정] 415 오류 해결: DTO 대신 JSON 문자열(String)로 받음
            @RequestPart("user") String userJson,

            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        try {
            // 💡 5. [핵심] 받아온 JSON 문자열(userJson)을 RegisterRequest DTO로 수동 변환
            RegisterRequest request = objectMapper.readValue(userJson, RegisterRequest.class);

            // 💡 6. 정상적으로 변환된 request 객체를 서비스로 전달
            LoginResponse response = userService.createUser(request, imageFiles);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (JsonProcessingException e) {
            // 💡 7. [추가] 'user' 파트의 JSON 형식이 잘못된 경우 (400 Bad Request)
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
    // 💡 8. [수정] 반환 타입을 ResponseEntity<User>로 변경 (Optional을 직접 노출하지 않음)
    public ResponseEntity<User> getUser(@PathVariable Long id) {

        // 💡 9. [수정] Service의 Optional 반환값을 처리하는 올바른 방법
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(user)) // 💡 .isPresent()
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND)); // 💡 .orElse()
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/login")
    // 💡 '/login'은 파일이 없으므로 @RequestBody로 JSON을 받는 것이 맞습니다.
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