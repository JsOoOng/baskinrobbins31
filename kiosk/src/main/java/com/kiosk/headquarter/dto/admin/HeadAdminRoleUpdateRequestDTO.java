package com.kiosk.headquarter.dto.admin;

import com.kiosk.entity.enums.AdminRole;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadAdminRoleUpdateRequestDTO {

    private AdminRole role;
}