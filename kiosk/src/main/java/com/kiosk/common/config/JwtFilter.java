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

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader != null
                && authorizationHeader.startsWith("Bearer ")) {

            String token =
                    authorizationHeader.substring(7);

            if (jwtUtil.validateToken(token)) {

                Integer employeeId =
                        jwtUtil.getEmployeeId(token);

                String role =
                        jwtUtil.getRole(token);

                String userType =
                        jwtUtil.getUserType(token);

                List<SimpleGrantedAuthority> authorities =
                        new ArrayList<>();

                /*
                 * Spring Security의 hasRole("HEAD_ADMIN")은
                 * 내부적으로 ROLE_HEAD_ADMIN을 검사합니다.
                 */
                if (role != null && !role.isBlank()) {
                    authorities.add(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + role
                            )
                    );
                }

                /*
                 * HEAD 또는 BRANCH 사용자 유형도 권한에 추가합니다.
                 *
                 * 예:
                 * TYPE_HEAD
                 * TYPE_BRANCH
                 */
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