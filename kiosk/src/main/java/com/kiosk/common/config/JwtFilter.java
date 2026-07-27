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

/**
 * [코드 흐름 안내] JwtFilter
 *
 * <p>역할: JWT 인증 요청이 Controller에 도달하기 전에 인증 정보와 접근 조건을 확인한다.</p>
 * <p>호출 흐름: HTTP 요청 -> 이 필터 -> SecurityContext 설정/검사 -> 허용된 Controller로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    /**
     * [메서드 흐름] doFilterInternal
     * HTTP 요청 필터 체인에서 호출되어 인증 정보를 확인한 뒤 다음 필터 또는 Controller로 요청을 넘긴다.
     */
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