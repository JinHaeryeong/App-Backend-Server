package com.dasom.dasomServer.Service;

import com.dasom.dasomServer.DAO.UserDAO;
import com.dasom.dasomServer.DTO.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {
    private final UserDAO userMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userMapper.findByLoginId(loginId);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLoginId())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}