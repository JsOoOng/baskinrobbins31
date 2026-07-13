package com.kiosk.headquarter.dto.security;

import java.time.LocalDateTime;

import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadAdminResponse {

    private Integer adminId;

    private String loginId;

    private String name;

    private String department;

    private AdminRole role;

    private AdminStatus status;

    private LocalDateTime createdAt;

    public static HeadAdminResponse from(
            HeadquarterAdmin admin
    ) {

        return HeadAdminResponse.builder()
                .adminId(admin.getId())
                .loginId(admin.getLoginId())
                .name(admin.getName())
                .department(admin.getDepartment())
                .role(admin.getRole())
                .status(admin.getStatus())
                .createdAt(admin.getCreatedAt())
                .build();
    }
}