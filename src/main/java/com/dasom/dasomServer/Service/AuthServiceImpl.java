package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DAO.UserMapper;
import com.dasom.dasomServer.DTO.LoginRequest;
import com.dasom.dasomServer.DTO.LoginResponse;
import com.dasom.dasomServer.DTO.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    @Override
    public LoginResponse authenticate(LoginRequest request) {
        // 1. ID로 사용자 정보 조회
        User user = userMapper.selectUserByLoginId(request.getLoginId());

        if (user == null) {
            return null;
        }

        // 2. 비밀번호 일치 확인
        if (request.getPassword().equals(user.getPassword())) {

            // 3. 인증 성공 시 응답 객체 생성 및 토큰 발급 (예시)
            return LoginResponse.builder()
                    .accessToken("sample_jwt_token_for_" + user.getLogin_id())
                    .userId(user.getUserId())
                    .username(user.getName())
                    .build();
        }

        return null;
    }
}