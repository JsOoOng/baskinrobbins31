package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.admin.HeadAdminCreateRequestDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminPasswordUpdateRequestDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminResponseDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminRoleUpdateRequestDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminStatusUpdateRequestDTO;
import com.kiosk.headquarter.dto.security.HeadAdminResponse;
import com.kiosk.headquarter.dto.security.HeadAdminUpdateRequest;
import com.kiosk.headquarter.service.HeadAdminSecurityService;
import com.kiosk.headquarter.service.HeadSecurityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadAdminSecurityController
 *
 * <p>역할: 본사 관리의 본사 관리자 계정 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> HeadAdminSecurityService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class HeadAdminSecurityController {

    private final HeadAdminSecurityService headAdminSecurityService;
    private final HeadSecurityService headSecurityService;

    @GetMapping("/head/admins")
    public List<HeadAdminResponseDTO> getAdminList() {
        return headAdminSecurityService.getAdminList();
    }

    @GetMapping("/head/admins/{adminId}")
    public HeadAdminResponseDTO getAdminDetail(@PathVariable Integer adminId) {
        return headAdminSecurityService.getAdminDetail(adminId);
    }

    /**
     * [요청 흐름] POST /head/admins
     */
    @PostMapping("/head/admins")
    public String createAdmin(
            @RequestBody @Valid HeadAdminCreateRequestDTO requestDTO 
    ) {
        return headAdminSecurityService.createAdmin(requestDTO);
    }

    /**
     * [요청 흐름] PUT /head/admins/{adminId}/role
     */
    @PutMapping("/head/admins/{adminId}/role")
    public String updateAdminRole(
            @PathVariable Integer adminId,
            @RequestBody @Valid HeadAdminRoleUpdateRequestDTO requestDTO 
    ) {
        return headAdminSecurityService.updateAdminRole(adminId, requestDTO);
    }

    @PutMapping("/head/admins/{adminId}")
    public HeadAdminResponse updateAdmin(
            Authentication authentication,
            @PathVariable Integer adminId,
            @RequestBody @Valid HeadAdminUpdateRequest request 
    ) {
        return headSecurityService.updateAdmin(
                (Integer) authentication.getPrincipal(),
                adminId,
                request
        );
    }

    /**
     * [요청 흐름] PUT /head/admins/{adminId}/status
     */
    @PutMapping("/head/admins/{adminId}/status")
    public String updateAdminStatus(
            @PathVariable Integer adminId,
            @RequestBody @Valid HeadAdminStatusUpdateRequestDTO requestDTO 
    ) {
        return headAdminSecurityService.updateAdminStatus(adminId, requestDTO);
    }

    /**
     * [요청 흐름] PUT /head/admins/{adminId}/password
     */
    @PutMapping("/head/admins/{adminId}/password")
    public String updateAdminPassword(
            @PathVariable Integer adminId,
            @RequestBody @Valid HeadAdminPasswordUpdateRequestDTO requestDTO 
    ) {
        return headAdminSecurityService.updateAdminPassword(adminId, requestDTO);
    }

    @DeleteMapping("/head/admins/{adminId}")
    public String deleteAdmin(@PathVariable Integer adminId,
                              @org.springframework.web.bind.annotation.RequestParam String confirmation) {
        return headAdminSecurityService.deleteAdmin(adminId, confirmation);
    }
}
