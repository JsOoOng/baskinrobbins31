package com.kiosk.headquarter.controller;

import com.kiosk.headquarter.dto.auth.HeadLoginRequest;
import com.kiosk.headquarter.dto.auth.HeadLoginResponse;
import com.kiosk.headquarter.dto.common.HeadApiResponse;
import com.kiosk.headquarter.service.HeadAuthService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/head/auth")
public class HeadAuthController {

    private final HeadAuthService headAuthService;

    public HeadAuthController(HeadAuthService headAuthService) {
        this.headAuthService = headAuthService;
    }

    @GetMapping("/")
    public String home() {
        return "kiosk server running";
    }

    @GetMapping("/head")
    public String headHome() {
        return "headquarter api running";
    }

    @PostMapping("/login")
    public HeadApiResponse<HeadLoginResponse> login(
            @RequestBody HeadLoginRequest request
    ) {
        HeadLoginResponse loginUser = headAuthService.login(request);

        return HeadApiResponse.ok("본사 관리자 로그인 성공", loginUser);
    }

    @GetMapping("/me")
    public HeadApiResponse<String> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return HeadApiResponse.fail("로그인 상태가 아닙니다.");
        }

        return HeadApiResponse.ok(
                "현재 로그인 중인 본사 관리자",
                authentication.getName()
        );
    }

    @PostMapping("/logout")
    public HeadApiResponse<Void> logout() {
        return HeadApiResponse.ok("프론트에서 JWT 토큰을 삭제해주세요.", null);
    }
}