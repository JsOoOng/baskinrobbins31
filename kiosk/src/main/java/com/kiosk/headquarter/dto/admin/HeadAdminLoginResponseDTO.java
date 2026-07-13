package com.kiosk.headquarter.dto.admin;

import com.kiosk.entity.enums.AdminRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadAdminLoginResponseDTO {

    private Integer adminId;
    private String loginId;
    private String name;
    private String department;
    private AdminRole role;
    private String token;
}