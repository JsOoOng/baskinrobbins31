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
import com.kiosk.common.config.JwtTokenStore;
import com.kiosk.common.config.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


/**
 * [코드 흐름 안내] AuthController
 *
 * <p>역할: 지점 운영의 인증 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch) -> AuthService, JwtUtil -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/branch")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final com.kiosk.common.service.TurnstileService turnstileService;
    private final JwtTokenStore jwtTokenStore;


    
    /**
     * [요청 흐름] POST /branch/login
     * 프론트 요청을 받아 login() 메서드가 입력을 받고 AuthService, JwtUtil 호출 후 결과를 응답한다.
     */
    @PostMapping(
    	    value = "/login",
    	    consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    	)
    public ResponseEntity<?> login(
    		@ModelAttribute @Valid AuthRequest request
    ){

        if (!turnstileService.verifyToken(request.getTurnstileToken())) {
            return ResponseEntity.badRequest().body(Map.of("message", "자동입력 방지(Turnstile) 검증에 실패했습니다. 새로고침 후 다시 시도해주세요."));
        }

    	AuthResponse user =
        		authService.login(request);


        String token =
            jwtUtil.createToken(user);
        
        jwtTokenStore.save(
                "BRANCH_" + user.getEmployeeId(),
                token
        );


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
                    "user", user
                )
            );
    }
    
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