package com.kiosk.common.config; // 패키지 선언

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // 이 클래스가 Spring의 설정(Configuration) 클래스임을 명시
@EnableWebSecurity // Spring Security 기능을 활성화
public class SecurityConfig {

    @Bean // 반환되는 객체를 Spring Bean으로 등록하여 컨테이너가 관리하게 함
    public SecurityFilterChain filterChain(
            HttpSecurity http, // HTTP 보안 설정을 구성할 수 있는 객체
            JwtUtil jwtUtil // JWT 토큰 생성 및 검증을 위한 커스텀 유틸리티 객체
    ) throws Exception {

        http
            // 1. CORS(교차 출처 리소스 공유) 설정
            .cors(cors -> cors.configurationSource(request -> {

                // CORS 설정 객체 생성
                var config = new org.springframework.web.cors.CorsConfiguration();

                // 모든 출처(Origin)에서의 접근을 허용 (예: 프론트엔드 도메인)
                config.setAllowedOriginPatterns(
                        java.util.List.of("*")
                );

                // 허용할 HTTP 메서드(GET, POST 등)를 명시적으로 지정
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

                // 클라이언트 요청 시 모든 HTTP 헤더를 허용
                config.setAllowedHeaders(
                        java.util.List.of("*")
                );

                // 자격 증명(쿠키, 인증 헤더 등)을 포함한 요청을 허용
                config.setAllowCredentials(true);

                return config; // 구성된 CORS 설정 반환
            }))

            // 2. CSRF 보호 비활성화: REST API 환경에서는 토큰 방식을 사용하므로 CSRF 보호가 불필요함
            .csrf(AbstractHttpConfigurer::disable)

            // 3. 세션 관리 설정: JWT를 사용하므로 서버에서 세션을 유지하지 않음
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS // 세션을 생성하지도 않고, 기존 세션을 사용하지도 않음
                    )
            )

            // 4. 커스텀 필터 등록: 기본 아이디/비밀번호 검증 필터가 실행되기 전에 JWT 필터를 먼저 실행
            .addFilterBefore(
                    new JwtFilter(jwtUtil), // 요청 헤더의 JWT를 검사하는 커스텀 필터
                    UsernamePasswordAuthenticationFilter.class // Spring Security의 기본 인증 필터
            )

            // 5. 엔드포인트(URL)별 접근 권한(인가) 설정
            .authorizeHttpRequests(auth -> auth

                    // 브라우저가 본 요청 전에 보내는 CORS Preflight(OPTIONS) 요청은 무조건 통과
                    .requestMatchers(
                            HttpMethod.OPTIONS,
                            "/**"
                    ).permitAll()

                    // 고객이 사용하는 공개 API는 인증 없이 접근 가능
                    .requestMatchers(
                            "/api/customer/**"
                    ).permitAll()

                    // 서버 상태를 확인하는 Actuator 엔드포인트 접근 허용
                    .requestMatchers(
                            "/actuator/**"
                    ).permitAll()

                    // 지점 직원의 로그인 엔드포인트 및 키오스크 조회/SSE 접근 허용 (키오스크 기기용)
                    .requestMatchers(
                            "/branch/login"
                    ).permitAll()
                    .requestMatchers(
                            HttpMethod.GET, "/branch/kiosk/**"
                    ).permitAll()

                    // 본사 직원의 로그인 엔드포인트는 인증 없이 접근 가능
                    .requestMatchers(
                            "/head/auth/login"
                    ).permitAll()
                    
                    // 쿠폰 등록
                    .requestMatchers(
                            "/head/coupon/**"
                    ).permitAll()

                    /*
                     * 최고 관리자 전용 API 구역
                     * Spring Security는 위에서부터 아래로 권한을 확인하므로 더 좁은 범위를 먼저 작성해야 함
                     */
                    .requestMatchers(
                            "/head/security/**",
                            "/head/admins/**"
                    ).hasRole("SUPER_ADMIN") // ROLE_SUPER_ADMIN 권한을 가진 사람만 접근 가능

                    // 일반 본사 관리자 기능 구역
                    .requestMatchers(
                            "/head/**"
                    ).hasAnyRole(
                            "ADMIN",       // 다음 세 가지 역할 중 하나라도 있으면 접근 가능
                            "HEAD_ADMIN",
                            "SUPER_ADMIN"
                    )

                    // 지점 직원 기능 구역
                    .requestMatchers(
                            "/branch/**"
                    ).hasAnyRole(
                            "MANAGER",     // 다음 두 가지 역할 중 하나라도 있으면 접근 가능
                            "STAFF"
                    )

                    // 위에서 명시적으로 설정하지 않은 나머지 모든 요청은 인증 없이 허용
                    .anyRequest().permitAll()
            )

            // 6. 사용하지 않는 기본 보안 기능 비활성화 (JWT를 사용하기 때문)
            .formLogin(AbstractHttpConfigurer::disable) // Spring Security 기본 로그인 폼 화면 비활성화
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 방식 비활성화
            .logout(AbstractHttpConfigurer::disable);   // 기본 로그아웃 처리 비활성화 (JWT 로그아웃은 보통 프론트에서 토큰 삭제로 처리)

        return http.build(); // 지금까지 설정한 내용으로 SecurityFilterChain을 완성하여 반환
    }
}