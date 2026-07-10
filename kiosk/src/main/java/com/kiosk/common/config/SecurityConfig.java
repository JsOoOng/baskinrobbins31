package com.kiosk.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http
	     	// 프론트엔드(5173)와의 통신 및 쿠키 공유를 허용합니다.
	        .cors(cors -> cors.configurationSource(request -> {
	            var config = new org.springframework.web.cors.CorsConfiguration();
	            config.setAllowedOrigins(java.util.List.of("http://localhost:5173")); // 프론트 주소
	            config.setAllowedMethods(
	            	    java.util.List.of(
	            	        "GET",
	            	        "POST",
	            	        "PUT",
	            	        "PATCH",
	            	        "DELETE",
	            	        "OPTIONS"
	            	    )
	            	);
	            config.setAllowedHeaders(java.util.List.of("*"));
	            config.setAllowCredentials(true); // 🚨 쿠키(세션) 공유 허용을 위해 필수!
	            return config;
	        }))
	        
            // 1. CSRF 방어 기능 비활성화 (Postman이나 curl로 POST 테스트를 하기 위해 필수)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. JWT 필터 추가 (지점 관리자 API 접근 시 토큰 검증용)
            // UsernamePasswordAuthenticationFilter 단계가 실행되기 전에 JwtFilter를 먼저 거치도록 설정합니다.
            .addFilterBefore(
                new JwtFilter(jwtUtil),
                UsernamePasswordAuthenticationFilter.class
            )
            
            // 3. URL 접근 권한 설정 (두 코드의 조건 완벽 통합)
            .authorizeHttpRequests(auth -> auth
                // 🔓 로그인 없이 누구나 접근 가능한 URL 목록
            	.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/customer/**").permitAll() // 고객용 장바구니/주문 API
                .requestMatchers("/head/**").permitAll()         // 본사 관련 테스트 API
                .requestMatchers("/actuator/**").permitAll()     // 서버 상태 체크용 API
                .requestMatchers("/branch/login").permitAll()    // 지점 관리자 로그인 API

                // 🔒 JWT 인증(로그인)이 필수인 URL 목록
                .requestMatchers("/branch/**").authenticated()   // 지점 관리자 전용 기능들

                // ⚠️ 그 외 나머지 모든 요청에 대한 처리 (anyRequest)
                // 지점 설정의 .permitAll()과 통합 규칙을 고려하여, 안전하게 authenticated()로 잠그거나 
                // 개발 편의를 위해 일단 permitAll()로 열어둘 수 있습니다. 여기서는 지점 코드의 철학에 맞춰 permitAll()로 매핑합니다.
                .anyRequest().permitAll()
            )

            // 4. API 서버에 불필요한 기본 보안 UI 끄기
            .formLogin(AbstractHttpConfigurer::disable) // Spring Security 기본 로그인 화면 끄기
            .httpBasic(AbstractHttpConfigurer::disable) // Postman에서 Basic Auth 요구하는 창 끄기
            .logout(AbstractHttpConfigurer::disable);   // 로그아웃 기본 설정 끄기

        return http.build();
    }
}