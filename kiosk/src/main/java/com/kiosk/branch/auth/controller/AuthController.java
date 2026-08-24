package com.kiosk.branch.auth.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;

import com.kiosk.branch.auth.dto.AuthRequest;
import com.kiosk.branch.auth.dto.AuthResponse;
import com.kiosk.branch.auth.service.AuthService;
import com.kiosk.branch.auth.service.LoginAttemptService;
import com.kiosk.common.config.JwtTokenStore;
import com.kiosk.common.config.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] AuthController
 *
 * <p>
 * 역할: 지점 운영의 인증 HTTP 요청을 받는 진입점이다.
 * </p>
 *
 * <p>
 * 호출 흐름:
 * Vue/API 요청
 * -> AuthController
 * -> AuthService
 * -> JwtUtil
 * -> JwtTokenStore
 * -> 응답
 * </p>
 *
 * <p>
 * 로그인 실패 횟수는 LoginAttemptService에서 관리한다.
 * </p>
 */
@RestController
@RequestMapping("/branch")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 로그인 인증 처리
     */
    private final AuthService authService;

    /**
     * JWT 생성
     */
    private final JwtUtil jwtUtil;

    /**
     * Cloudflare Turnstile 검증
     */
    private final com.kiosk.common.service.TurnstileService turnstileService;

    /**
     * JWT 저장소
     */
    private final JwtTokenStore jwtTokenStore;

    /**
     * 로그인 실패 횟수 및 일시적 차단 관리
     */
    private final LoginAttemptService loginAttemptService;


    /**
     * [요청 흐름] POST /branch/login
     *
     * <p>
     * Vue 로그인 요청을 받아
     * Turnstile 검증 -> AuthService 인증 -> JWT 생성 -> 토큰 저장
     * 순서로 처리한다.
     * </p>
     *
     * <p>
     * 로그인 실패 시 현재 실패 횟수를 함께 반환한다.
     * </p>
     */
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<?> login(
            @ModelAttribute @Valid AuthRequest request
    ) {

        /*
         * =========================================================
         * 1. 로그인 ID 정리
         * =========================================================
         */
        String loginId = request.getLoginId() == null
                ? ""
                : request.getLoginId().trim();


        /*
         * =========================================================
         * 2. Turnstile 검증
         * =========================================================
         */
        if (!turnstileService.verifyToken(
                request.getTurnstileToken()
        )) {

            return ResponseEntity.badRequest().body(
                    Map.of(
                            "code", "BAD_REQUEST",
                            "message",
                            "자동입력 방지(Turnstile) 검증에 실패했습니다. 새로고침 후 다시 시도해주세요."
                    )
            );
        }


        /*
         * =========================================================
         * 3. 이미 로그인 차단 상태인지 확인
         * =========================================================
         *
         * 실제 존재하는 ID에 대해서만 LoginAttemptService에
         * 실패 기록이 존재한다.
         */
        if (loginAttemptService.isBlocked(loginId)) {

            return ResponseEntity.status(429).body(
                    Map.of(
                            "code", "LOGIN_BLOCKED",
                            "message",
                            "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.",
                            "failedCount",
                            loginAttemptService.getFailedCount(loginId),
                            "maxAttempts",
                            5,
                            "blocked",
                            true
                    )
            );
        }


        /*
         * =========================================================
         * 4. 실제 로그인 인증
         * =========================================================
         */
        try {

            AuthResponse user = authService.login(request);

            /*
             * =====================================================
             * 5. JWT 생성
             * =====================================================
             */
            String token = jwtUtil.createToken(user);

            /*
             * =====================================================
             * 6. JWT 저장
             * =====================================================
             */
            jwtTokenStore.save(
                    "BRANCH_" + user.getEmployeeId(),
                    token
            );

            /*
             * =====================================================
             * 7. 로그인 성공
             * =====================================================
             *
             * AuthService에서 이미 실패 기록을 초기화한다.
             */
            ResponseCookie cookie = ResponseCookie.from("branchToken", token)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(72000) // 20시간
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(
                            Map.of(
                                    "token", token,
                                    "user", user,
                                    "failedCount", 0,
                                    "maxAttempts", 5,
                                    "blocked", false
                            )
                    );

        } catch (IllegalArgumentException e) {

            /*
             * =====================================================
             * 로그인 실패
             * =====================================================
             *
             * AuthService 내부에서 비밀번호가 틀린 경우
             * LoginAttemptService.loginFailed()가 호출된다.
             *
             * 따라서 여기에서는 현재 실패 횟수만 조회한다.
             */

            int failedCount =
                    loginAttemptService.getFailedCount(loginId);


            /*
             * 5회 실패하여 차단된 경우
             */
            if (loginAttemptService.isBlocked(loginId)) {

                return ResponseEntity.status(429).body(
                        Map.of(
                                "code", "LOGIN_BLOCKED",
                                "message",
                                "로그인 시도 횟수를 초과했습니다. 30초 후 다시 시도해주세요.",
                                "failedCount",
                                failedCount,
                                "maxAttempts",
                                5,
                                "blocked",
                                true
                        )
                );
            }


            /*
             * 아직 5회 미만인 경우
             */
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "code", "LOGIN_FAILED",
                            "message",
                            "로그인에 실패하였습니다.",
                            "failedCount",
                            failedCount,
                            "maxAttempts",
                            5,
                            "blocked",
                            false
                    )
            );
        }
    }


    /**
     * [요청 흐름] POST /branch/logout
     *
     * <p>
     * Authorization 헤더의 JWT를 확인하여
     * 서버의 JWT 저장소에서 해당 토큰을 제거한다.
     * </p>
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = "branchToken", required = false) String token,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String actualToken = token;
        if (actualToken == null && authorization != null && authorization.startsWith("Bearer ")) {
            actualToken = authorization.substring(7);
        }

        if (actualToken != null) {
            Integer employeeId = jwtUtil.getEmployeeId(actualToken);
            jwtTokenStore.remove("BRANCH_" + employeeId);
        }

        ResponseCookie cookie = ResponseCookie.from("branchToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(
                        Map.of(
                                "message",
                                "로그아웃 완료"
                        )
                );
    }
}