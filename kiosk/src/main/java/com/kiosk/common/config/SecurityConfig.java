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
    public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();

                config.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
                config.setAllowedMethods(java.util.List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                ));
                config.setAllowedHeaders(java.util.List.of("*"));
                config.setAllowCredentials(true);

                return config;
            }))

            .csrf(AbstractHttpConfigurer::disable)

            // JWT 방식이므로 서버 세션 사용 안 함
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .addFilterBefore(
                    new JwtFilter(jwtUtil),
                    UsernamePasswordAuthenticationFilter.class
            )

            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // 고객 API 공개
                    .requestMatchers("/api/customer/**").permitAll()

                    // 서버 상태 체크 공개
                    .requestMatchers("/actuator/**").permitAll()

                    // 지점 로그인 공개
                    .requestMatchers("/branch/login").permitAll()

                    // 본사 로그인 공개
                    .requestMatchers("/head/auth/login").permitAll()

                    // 지점 기능은 JWT 필요
                    .requestMatchers("/branch/**").authenticated()

                    // 본사 기능도 JWT 필요
                    .requestMatchers("/head/**").authenticated()

                    // 나머지는 개발 편의상 허용
                    .anyRequest().permitAll()
            )

            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}