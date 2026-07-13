package com.kiosk.headquarter.dto.security;

import com.kiosk.entity.enums.AdminRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadAdminUpdateRequest {

    private String name;

    private String department;

    private AdminRole role;
}