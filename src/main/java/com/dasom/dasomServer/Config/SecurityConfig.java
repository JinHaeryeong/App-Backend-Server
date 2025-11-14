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

    @Value("${file.access-path}")
    private String accessPath; // 예: "/uploads/"

    @Lazy
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // accessPath 경로와 그 하위 경로(**)의 보안 검사를 완전히 제외합니다.
        return (web) -> web.ignoring().requestMatchers(accessPath + "**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 💡 CSRF 보호를 명확하게 비활성화합니다.
                //    이것이 이전 로그에서 확인된 403 Forbidden (CSRF token error)를 해결합니다.
                .csrf(csrf -> csrf.disable())

                // 기존 설정 유지
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 인증 및 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 1. **가장 구체적인** 인증 면제 경로 설정 (permitAll)
                        .requestMatchers(HttpMethod.POST, "/api/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()

                        // 2. **구체적인** 역할(Role) 기반 경로 설정
                        //    -> /api/caregivers/by-silver/** 가 /api/caregivers/** 보다 먼저 나와야 합니다.
                        .requestMatchers("/api/caregivers/by-silver/**").hasAnyRole("USER", "ADMIN")

                        // 3. 일반 인증 기반 경로 설정 (Role 대신 authenticated() 사용)
                        //    -> /api/guardians/** 경로에 대한 hasAnyRole 규칙은 authenticated()로 대체하거나
                        //       가장 광범위한 authenticated() 규칙이 처리하도록 합니다.
                        .requestMatchers("/api/caregivers/**").authenticated()
                        .requestMatchers("/api/guardians/**").authenticated()
                        .requestMatchers("/api/medications/**").permitAll()

                        // 4. 나머지 모든 요청은 인증 필요 (가장 광범위한 규칙은 맨 마지막에)
                        .anyRequest().authenticated()
                )
                // JWT 필터 추가
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

        // 이미지 경로(accessPath)에 대해서도 CORS 설정을 적용합니다.
        source.registerCorsConfiguration(accessPath + "**", configuration);

        return source;
    }
}