package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.common.HeadApiResponse;
import com.kiosk.headquarter.dto.security.HeadAdminCreateRequest;
import com.kiosk.headquarter.dto.security.HeadAdminPasswordRequest;
import com.kiosk.headquarter.dto.security.HeadAdminResponse;
import com.kiosk.headquarter.dto.security.HeadAdminStatusRequest;
import com.kiosk.headquarter.dto.security.HeadAdminUpdateRequest;
import com.kiosk.headquarter.service.HeadSecurityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/head/admins")
@RequiredArgsConstructor
public class HeadSecurityController {

    private final HeadSecurityService
            headSecurityService;

    /*
     * 관리자 목록
     */
    @GetMapping
    public HeadApiResponse<List<HeadAdminResponse>>
            getAdmins() {

        return HeadApiResponse.ok(
                "본사 관리자 목록 조회 성공",
                headSecurityService.getAdmins()
        );
    }

    /*
     * 관리자 상세
     */
    @GetMapping("/{adminId}")
    public HeadApiResponse<HeadAdminResponse>
            getAdmin(
                    @PathVariable
                    Integer adminId
            ) {

        return HeadApiResponse.ok(
                "본사 관리자 상세 조회 성공",
                headSecurityService.getAdmin(
                        adminId
                )
        );
    }

    /*
     * 관리자 생성
     */
    @PostMapping
    public HeadApiResponse<HeadAdminResponse>
            createAdmin(
                    @RequestBody
                    HeadAdminCreateRequest request
            ) {

        return HeadApiResponse.ok(
                "본사 관리자 계정 생성 성공",
                headSecurityService.createAdmin(
                        request
                )
        );
    }

    /*
     * 관리자 정보 수정
     */
    @PutMapping("/{adminId}")
    public HeadApiResponse<HeadAdminResponse>
            updateAdmin(
                    @PathVariable
                    Integer adminId,

                    @RequestBody
                    HeadAdminUpdateRequest request,

                    Authentication authentication
            ) {

        Integer currentAdminId =
                getCurrentAdminId(
                        authentication
                );

        return HeadApiResponse.ok(
                "본사 관리자 정보 수정 성공",
                headSecurityService.updateAdmin(
                        currentAdminId,
                        adminId,
                        request
                )
        );
    }

    /*
     * 계정 상태 변경
     */
    @PatchMapping("/{adminId}/status")
    public HeadApiResponse<HeadAdminResponse>
            changeStatus(
                    @PathVariable
                    Integer adminId,

                    @RequestBody
                    HeadAdminStatusRequest request,

                    Authentication authentication
            ) {

        Integer currentAdminId =
                getCurrentAdminId(
                        authentication
                );

        return HeadApiResponse.ok(
                "본사 관리자 상태 변경 성공",
                headSecurityService.changeStatus(
                        currentAdminId,
                        adminId,
                        request
                )
        );
    }

    /*
     * 비밀번호 초기화
     */
    @PatchMapping("/{adminId}/password")
    public HeadApiResponse<Void>
            resetPassword(
                    @PathVariable
                    Integer adminId,

                    @RequestBody
                    HeadAdminPasswordRequest request
            ) {

        headSecurityService.resetPassword(
                adminId,
                request
        );

        return HeadApiResponse.ok(
                "관리자 비밀번호 초기화 성공",
                null
        );
    }

    private Integer getCurrentAdminId(
            Authentication authentication
    ) {

        if (
                authentication == null ||
                !authentication.isAuthenticated()
        ) {
            throw new IllegalArgumentException(
                    "로그인이 필요합니다."
            );
        }

        try {
            return Integer.valueOf(
                    authentication.getName()
            );

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "관리자 인증 정보가 올바르지 않습니다."
            );
        }
    }
}