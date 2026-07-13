package com.kiosk.headquarter.dto.security;

import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadAdminCreateRequest {

    private String loginId;

    private String password;

    private String name;

    private String department;

    private AdminRole role;

    private AdminStatus status;
}