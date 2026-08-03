package com.kiosk.headquarter.dto.admin;

import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadAdminUpdateRequestDTO {
    private String name;
    private String department;
    private AdminRole role;
    private AdminStatus status;
}
