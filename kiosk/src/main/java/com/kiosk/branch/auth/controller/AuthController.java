package com.kiosk.branch.auth.controller;



import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.auth.dto.AuthRequest;
import com.kiosk.branch.auth.dto.AuthResponse;
import com.kiosk.branch.auth.service.AuthService;
import com.kiosk.common.config.JwtUtil;

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



    
    /**
     * [요청 흐름] POST /branch/login
     * 프론트 요청을 받아 login() 메서드가 입력을 받고 AuthService, JwtUtil 호출 후 결과를 응답한다.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AuthRequest request
    ){

    	AuthResponse user =
        		authService.login(request);


        String token =
            jwtUtil.createToken(user);


        return ResponseEntity.ok(
            Map.of(
                "token", token,
                "user", user
            )
        );
    }
    
    /*  vue에서 관리시 불필요 기능
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {

        session.invalidate();

        return ResponseEntity.ok("로그아웃 완료");
    }
	*/
	
}