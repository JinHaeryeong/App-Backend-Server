package com.dasom.dasomServer.service;

import com.dasom.dasomServer.dto.User;
import com.dasom.dasomServer.dao.UserDAO;
import com.dasom.dasomServer.dto.LoginRequest;
import com.dasom.dasomServer.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserDAO userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse authenticate(LoginRequest request) {
        User user = userMapper.selectUserByLoginId(request.getLoginId());
        log.info("User: {}", user);
        if (user == null) {
            return null;
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return LoginResponse.builder()
                    .accessToken("sample_jwt_token_for_" + user.getLoginid())
                    .userId(user.getId())
                    .username(user.getName())
                    .gender(user.getGender())
                    .build();
        }
        return null;

    }
}