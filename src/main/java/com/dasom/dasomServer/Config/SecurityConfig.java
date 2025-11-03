package com.dasom.dasomServer.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // [핵심] CSRF 비활성화: 403 Forbidden의 가장 흔한 원인.
                .csrf(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증과 폼 로그인을 사용하지 않도록 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // [핵심] 세션 비활성화: JWT/토큰 기반 인증을 위한 Stateless 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 회원가입 및 로그인 POST 요청 명시적 허용 (인증 없이 접근 가능)
                        .requestMatchers(HttpMethod.POST, "/api/signup", "/api/login").permitAll()

                        // CORS 사전 요청 (Pre-flight) OPTIONS 메서드 모두 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 그 외 모든 요청은 인증 토큰이 필요함
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 모든 출처(Origin), 메서드, 헤더 허용 (개발 및 테스트 환경에 적합)
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // /api/** 경로에 대해 CORS 설정 적용
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}