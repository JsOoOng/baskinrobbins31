package com.kiosk.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // POST, PUT, DELETE 테스트할 때 CSRF 때문에 막히는 것 방지
            .csrf(csrf -> csrf.disable())

            // URL 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/head/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().permitAll()
            )

            // Spring Security 기본 로그인 화면 끄기
            .formLogin(form -> form.disable())

            // Postman에서 Basic Auth 요구하는 것 끄기
            .httpBasic(basic -> basic.disable())

            // 로그아웃 기본 설정 끄기
            .logout(logout -> logout.disable());

        return http.build();
    }
}