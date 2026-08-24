package com.kiosk.common.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final JwtTokenStore jwtTokenStore;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String token = null;

        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        if (token == null && request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("headToken".equals(cookie.getName()) || "branchToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token != null) {
            // 1. JWT 기본 검증
            if (jwtUtil.validateToken(token)) {


                Integer employeeId =
                        jwtUtil.getEmployeeId(token);


                String userType =
                        jwtUtil.getUserType(token);


                String tokenKey =
                        userType + "_" + employeeId;


                /*
                 * 현재 로그인 토큰 확인
                 *
                 * 예:
                 * HEAD_1
                 * BRANCH_1
                 *
                 * 동일 계정 재로그인 시
                 * 기존 JWT는 여기서 차단
                 */
                if (!jwtTokenStore.isValid(
                        tokenKey,
                        token
                )) {

                    response.setStatus(
                            HttpServletResponse.SC_UNAUTHORIZED
                    );

                    return;
                }


                String role =
                        jwtUtil.getRole(token);


                List<SimpleGrantedAuthority> authorities =
                        new ArrayList<>();


                if (role != null && !role.isBlank()) {

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + role
                            )
                    );
                }


                if (userType != null && !userType.isBlank()) {

                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "TYPE_" + userType
                            )
                    );
                }


                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                employeeId,
                                null,
                                authorities
                        );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }
        }


        filterChain.doFilter(
                request,
                response
        );
    }
}