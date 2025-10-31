package com.dasom.dasomServer.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API 사용을 위해 CSRF, HTTP Basic, 폼 로그인 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // 세션 미사용 설정 (토큰 기반 인증)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 인증 및 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 로그인과 회원가입 경로는 인증 없이 접근 허용
                        // 죄송한데 실험용으로 걍 전부 허용할게요
                                .anyRequest().permitAll()
//                        .requestMatchers("/api/auth/**", "/api/users/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
//                        .anyRequest().authenticated()
                );
        return http.build();
    }
}