package com.dasom.dasomServer.service;

import com.dasom.dasomServer.dao.UserDAO;
import com.dasom.dasomServer.dto.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDAO userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void createUser(User user) {
        if (userMapper.existsByLoginId(user.getLoginid()) > 0) {
            throw new IllegalStateException("이미 존재하는 아이디입니다: " + user.getLoginid());
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        userMapper.insertUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByLoginId(String loginId) {
        return Optional.ofNullable(userMapper.selectUserByLoginId(loginId));
    }


    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userMapper.selectUserById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userMapper.selectAllUsers();
    }


}