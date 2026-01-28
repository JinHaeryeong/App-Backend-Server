package com.dasom.dasomServer.domain.silver.service;

import com.dasom.dasomServer.domain.silver.dto.LoginResponse;
import com.dasom.dasomServer.domain.silver.dto.RegisterRequest;
import com.dasom.dasomServer.domain.silver.dto.SilverResponse;
import com.dasom.dasomServer.domain.silver.entity.Silver;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface UserService {

    // 여기를 Impl 클래스와 동일하게 수정
    LoginResponse createUser(RegisterRequest request, List<MultipartFile> imageFiles);

    // 로그인 ID로 사용자 조회 (인증 / ID 중복 검사)
    Optional<SilverResponse> getUserByLoginId(String loginId);

    Optional<SilverResponse> getUserById(Long id);

    List<SilverResponse> getAllUsers();

    // 로그인 인증
    LoginResponse authenticateUser(String loginId, String password);
}