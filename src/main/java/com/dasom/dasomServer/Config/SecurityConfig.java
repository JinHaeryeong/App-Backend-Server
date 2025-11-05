package com.dasom.dasomServer.Config;

import com.dasom.dasomServer.Security.JwtAuthenticationFilter;
import com.dasom.dasomServer.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // [수정] @Value 임포트
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer; // [수정] WebSecurityCustomizer 임포트
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

    // [수정] yml의 'file.access-path' 값을 주입받는 코드가 누락되어 추가합니다.
    @Value("${file.access-path}")
    private String accessPath; // "/uploads/"

    @Lazy
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인을 무시하는 경로를 설정합니다. (403 해결)
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // accessPath 변수를 사용하기 위해 @Value 선언이 반드시 필요합니다.
        return (web) -> web.ignoring().requestMatchers(accessPath + "**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 인증 및 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers("/api/health/**").authenticated()
                        // "/api/guardians/"로 시작하는 모든 주소(/**)에 대해 접근을 허용합니다.
                        .requestMatchers("/api/guardians/**").permitAll()
                        // [수정] 하드코딩된 "/images/**" 대신 accessPath 변수를 사용합니다.
                        .requestMatchers(HttpMethod.GET, accessPath + "**").permitAll()
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

        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // /api/** 경로에 대해 CORS 설정 적용
        source.registerCorsConfiguration("/api/**", configuration);

        // [수정] 이미지 경로(accessPath)에 대해서도 CORS 설정을 반드시 추가해야 합니다.
        source.registerCorsConfiguration(accessPath + "**", configuration);

        return source;
    }
}