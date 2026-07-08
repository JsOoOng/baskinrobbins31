package com.kiosk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. CSRF 방어 기능 비활성화 (Postman이나 curl로 POST 테스트를 하기 위해 필수)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. 요청 주소별 권한 설정
            .authorizeHttpRequests(auth -> auth
                // /api/customer/ 하위의 모든 주소는 로그인 없이 누구나(permitAll) 접근 가능
                .requestMatchers("/api/customer/**").permitAll()
                // 그 외의 나머지 주소(지점, 본사 등)는 로그인(인증) 필요
                .anyRequest().authenticated()
            );

        return http.build();
    }
}