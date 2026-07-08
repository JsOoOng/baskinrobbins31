package com.kiosk.headquarter.controller;

import com.kiosk.headquarter.dto.auth.HeadLoginRequest;
import com.kiosk.headquarter.dto.auth.HeadLoginResponse;
import com.kiosk.headquarter.dto.common.HeadApiResponse;
import com.kiosk.headquarter.service.HeadAuthService;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/head/auth")
public class HeadAuthController {
	
	@GetMapping("/")
    public String home() {
        return "kiosk server running";
    }

    @GetMapping("/head")
    public String headHome() {
        return "headquarter api running";
    }

    private final HeadAuthService headAuthService;

    public HeadAuthController(HeadAuthService headAuthService) {
        this.headAuthService = headAuthService;
    }

    @PostMapping("/login")
    public HeadApiResponse<HeadLoginResponse> login(
            @RequestBody HeadLoginRequest request,
            HttpSession session
    ) {
        HeadLoginResponse loginUser = headAuthService.login(request);

        // 세션에 본사 관리자 정보 저장
        session.setAttribute("HEAD_EMPLOYEE_ID", loginUser.getEmployeeId());
        session.setAttribute("HEAD_LOGIN_ID", loginUser.getLoginId());
        session.setAttribute("HEAD_ROLE", loginUser.getRole());

        return HeadApiResponse.ok("본사 관리자 로그인 성공", loginUser);
    }

    @GetMapping("/me")
    public HeadApiResponse<String> me(HttpSession session) {
        Object loginId = session.getAttribute("HEAD_LOGIN_ID");

        if (loginId == null) {
            return HeadApiResponse.fail("로그인 상태가 아닙니다.");
        }

        return HeadApiResponse.ok("현재 로그인 중인 본사 관리자", loginId.toString());
    }

    @PostMapping("/logout")
    public HeadApiResponse<Void> logout(HttpSession session) {
        session.invalidate();
        return HeadApiResponse.ok("본사 관리자 로그아웃 성공", null);
    }
}