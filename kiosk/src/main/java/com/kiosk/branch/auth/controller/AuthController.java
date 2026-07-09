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


@RestController
@RequestMapping("/branch")
@RequiredArgsConstructor
public class AuthController {


    private final AuthService authService;
    private final JwtUtil jwtUtil;



    
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