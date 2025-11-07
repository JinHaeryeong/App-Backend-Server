package com.dasom.dasomServer.Config;

import com.dasom.dasomServer.Security.JwtAuthenticationFilter;
import com.dasom.dasomServer.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Lazy
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // 세션 미사용 설정 (토큰 기반 인증)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 1. 회원가입/로그인 허용 (토큰 불필요)
                        .requestMatchers(HttpMethod.POST, "/api/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers("/api/health/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                        .requestMatchers("/api/guardians/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/caregivers/by-silver/**").hasAnyRole("USER", "ADMIN")

                        // 2. 보호자 API는 인증된 사용자만 접근 허용
                        //    (토큰이 유효해야 403 에러가 해결됩니다.)
                        .requestMatchers("/api/guardians/**").authenticated()
                        .requestMatchers("/api/caregivers/**").permitAll()

                                // 로그인과 회원가입 경로는 인증 없이 접근 허용
                        // 죄송한데 실험용으로 걍 전부 허용할게요
//                                .anyRequest().permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("*")); // 개발 환경에서 전체 허용
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // /api/** 경로에 대해 CORS 설정 적용
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}