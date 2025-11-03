package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DTO.LoginResponse;
import com.dasom.dasomServer.DTO.RegisterRequest;
import com.dasom.dasomServer.DTO.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface UserService {

    @Transactional
    LoginResponse createUser(RegisterRequest request, MultipartFile profileImage);

    // 로그인 ID로 사용자 조회 (인증 / ID 중복 검사)
    Optional<User> getUserByLoginId(String loginId);

    Optional<User> getUserById(Long id);

    List<User> getAllUsers();

    // 로그인 인증
    LoginResponse authenticateUser(String loginId, String password);
}