package com.kiosk.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtUtil jwtUtil
    ) throws Exception {

        http
            .cors(cors -> cors.configurationSource(request -> {

                var config =
                        new org.springframework.web.cors.CorsConfiguration();

                config.setAllowedOrigins(
                        java.util.List.of(
                                "http://localhost:5173"
                        )
                );

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

                config.setAllowedHeaders(
                        java.util.List.of("*")
                );

                config.setAllowCredentials(true);

                return config;
            }))

            .csrf(AbstractHttpConfigurer::disable)

            // JWT 사용: 서버 세션 생성하지 않음
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            .addFilterBefore(
                    new JwtFilter(jwtUtil),
                    UsernamePasswordAuthenticationFilter.class
            )

            .authorizeHttpRequests(auth -> auth

                    // 브라우저 CORS 사전 요청
                    .requestMatchers(
                            HttpMethod.OPTIONS,
                            "/**"
                    ).permitAll()

                    // 고객 공개 API
                    .requestMatchers(
                            "/api/customer/**"
                    ).permitAll()

                    // 서버 상태 확인
                    .requestMatchers(
                            "/actuator/**"
                    ).permitAll()

                    // 지점 로그인 공개
                    .requestMatchers(
                            "/branch/login"
                    ).permitAll()

                    // 본사 로그인 공개
                    .requestMatchers(
                            "/head/auth/login"
                    ).permitAll()

                    /*
                     * 최고 관리자만 접근
                     * 반드시 일반 본사 권한보다 위에 작성
                     */
                    .requestMatchers(
                            "/head/security/**",
                            "/head/admins/**"
                    ).hasRole("SUPER_ADMIN")

                    // 본사 관리자 기능
                    .requestMatchers(
                            "/head/**"
                    ).hasAnyRole(
                            "HEAD_ADMIN",
                            "SUPER_ADMIN"
                    )

                    // 지점 기능
                    .requestMatchers(
                            "/branch/**"
                    ).hasAnyRole(
                            "STORE_ADMIN",
                            "STAFF"
                    )

                    .anyRequest().permitAll()
            )

            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}