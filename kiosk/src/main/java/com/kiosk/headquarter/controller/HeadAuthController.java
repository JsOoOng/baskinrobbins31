package com.kiosk.headquarter.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.kiosk.headquarter.dto.auth.HeadLoginRequest;
import com.kiosk.headquarter.dto.auth.HeadLoginResponse;
import com.kiosk.headquarter.dto.common.HeadApiResponse;
import com.kiosk.headquarter.service.HeadAuthService;

/**
 * [코드 흐름 안내] HeadAuthController
 *
 * <p>역할: 본사 관리의 본사 인증 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/auth) -> HeadAuthService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/auth")
public class HeadAuthController {

    private final HeadAuthService headAuthService;

    public HeadAuthController(HeadAuthService headAuthService) {
        this.headAuthService = headAuthService;
    }

    /*
     * 본사 인증 API 상태 확인
     *
     * 주소:
     * GET /head/auth
     */
    /**
     * [요청 흐름] GET /head/auth
     * 프론트 요청을 받아 home() 메서드가 입력을 받고 HeadAuthService 호출 후 결과를 응답한다.
     */
    @GetMapping({"", "/"})
    public String home() {
        return "headquarter auth api running";
    }

    /*
     * 본사 관리자 로그인
     *
     * 주소:
     * POST /head/auth/login
     */
    /**
     * [요청 흐름] POST /head/auth/login
     * 프론트 요청을 받아 login() 메서드가 입력을 받고 HeadAuthService 호출 후 결과를 응답한다.
     */
    @PostMapping("/login")
    public HeadApiResponse<HeadLoginResponse> login(
            @RequestBody HeadLoginRequest request
    ) {

        HeadLoginResponse loginUser =
                headAuthService.login(request);

        return HeadApiResponse.ok(
                "본사 관리자 로그인 성공",
                loginUser
        );
    }

    /*
     * 현재 로그인한 본사 관리자 정보 조회
     *
     * 주소:
     * GET /head/auth/me
     *
     * Authorization:
     * Bearer JWT토큰
     */
    /**
     * [요청 흐름] GET /head/auth/me
     * 프론트 요청을 받아 me() 메서드가 입력을 받고 HeadAuthService 호출 후 결과를 응답한다.
     */
    @GetMapping("/me")
    public HeadApiResponse<HeadLoginResponse> me(
            Authentication authentication
    ) {

        // 1. authentication을 사용하기 전에 먼저 검사
        if (authentication == null
                || !authentication.isAuthenticated()) {

            return HeadApiResponse.fail(
                    "로그인 상태가 아닙니다."
            );
        }

        Integer employeeId;

        try {
            /*
             * JwtFilter에서 principal 자리에
             * employeeId를 넣었으므로 getName()은
             * 직원 번호 문자열을 반환합니다.
             */
            employeeId =
                    Integer.valueOf(authentication.getName());

        } catch (NumberFormatException e) {

            return HeadApiResponse.fail(
                    "로그인 인증 정보가 올바르지 않습니다."
            );
        }

        // 2. 직원 번호로 현재 관리자 정보 조회
        HeadLoginResponse loginUser =
                headAuthService.getLoginUser(employeeId);

        // 3. 직원 번호 문자열이 아닌 관리자 전체 정보 반환
        return HeadApiResponse.ok(
                "현재 로그인 중인 본사 관리자",
                loginUser
        );
    }

    /*
     * 본사 관리자 로그아웃
     *
     * JWT 방식이므로 서버 세션을 삭제하지 않습니다.
     * 프론트엔드에서 localStorage의 토큰을 제거합니다.
     *
     * 주소:
     * POST /head/auth/logout
     */
    /**
     * [요청 흐름] POST /head/auth/logout
     * 프론트 요청을 받아 logout() 메서드가 입력을 받고 HeadAuthService 호출 후 결과를 응답한다.
     */
    @PostMapping("/logout")
    public HeadApiResponse<Void> logout() {

        return HeadApiResponse.ok(
                "로그아웃되었습니다. 프론트에서 JWT 토큰을 삭제해주세요.",
                null
        );
    }
}