package com.kiosk.headquarter.dto.admin;

import java.time.LocalDateTime;

import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadAdminResponseDTO {

    private Integer adminId;
    private String loginId;
    private String name;
    private String department;
    private AdminRole role;
    private AdminStatus status;
    private LocalDateTime createdAt;
}