package com.kiosk.headquarter.dto.security;

import com.kiosk.entity.enums.AdminRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadAdminUpdateRequest {

    /*
     * 관리자 이름
     */
    private String name;

    /*
     * 소속 부서
     */
    private String department;

    /*
     * 관리자 역할
     */
    private AdminRole role;
}