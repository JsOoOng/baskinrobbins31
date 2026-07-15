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

    /*
     * 관리자 로그인 ID
     */
    private String loginId;

    /*
     * 초기 비밀번호
     */
    private String password;

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
     *
     * SUPER_ADMIN
     * HEAD_ADMIN
     * ADMIN
     */
    private AdminRole role;

    /*
     * 계정 상태
     *
     * ACTIVE
     * INACTIVE
     */
    private AdminStatus status;
}