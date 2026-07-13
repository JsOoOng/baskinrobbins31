package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.admin.HeadAdminCreateRequestDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminPasswordUpdateRequestDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminResponseDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminRoleUpdateRequestDTO;
import com.kiosk.headquarter.dto.admin.HeadAdminStatusUpdateRequestDTO;
import com.kiosk.headquarter.service.HeadAdminSecurityService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HeadAdminSecurityController {

    private final HeadAdminSecurityService headAdminSecurityService;

    @GetMapping("/head/admins")
    public List<HeadAdminResponseDTO> getAdminList() {
        return headAdminSecurityService.getAdminList();
    }

    @GetMapping("/head/admins/{adminId}")
    public HeadAdminResponseDTO getAdminDetail(@PathVariable Integer adminId) {
        return headAdminSecurityService.getAdminDetail(adminId);
    }

    @PostMapping("/head/admins")
    public String createAdmin(@RequestBody HeadAdminCreateRequestDTO requestDTO) {
        return headAdminSecurityService.createAdmin(requestDTO);
    }

    @PutMapping("/head/admins/{adminId}/role")
    public String updateAdminRole(
            @PathVariable Integer adminId,
            @RequestBody HeadAdminRoleUpdateRequestDTO requestDTO) {

        return headAdminSecurityService.updateAdminRole(adminId, requestDTO);
    }

    @PutMapping("/head/admins/{adminId}/status")
    public String updateAdminStatus(
            @PathVariable Integer adminId,
            @RequestBody HeadAdminStatusUpdateRequestDTO requestDTO) {

        return headAdminSecurityService.updateAdminStatus(adminId, requestDTO);
    }

    @PutMapping("/head/admins/{adminId}/password")
    public String updateAdminPassword(
            @PathVariable Integer adminId,
            @RequestBody HeadAdminPasswordUpdateRequestDTO requestDTO) {

        return headAdminSecurityService.updateAdminPassword(adminId, requestDTO);
    }
}