package com.dasom.dasomServer.domain.silver.service;

import com.dasom.dasomServer.domain.silver.entity.Silver;
import com.dasom.dasomServer.domain.silver.repository.SilverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {
    private final SilverRepository silverRepository;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // Optional이라서 .orElseThrow로 한 방에 처리 가능
        Silver silver = silverRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

        return org.springframework.security.core.userdetails.User.builder()
                .username(silver.getLoginId())
                .password(silver.getPassword())
                .roles("USER")
                .build();
    }
}