package com.dasom.dasomServer.service;

import com.dasom.dasomServer.dto.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void createUser(User user);

    // R: 로그인 ID로 사용자 조회 (인증용)
    Optional<User> getUserByLoginId(String loginId);

    // R: ID로 사용자 단건 조회
    User getUserById(Long id);

    // R: 전체 사용자 목록 조회
    List<User> getAllUsers();
}