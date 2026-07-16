package com.kiosk.headquarter.dto.security;

import com.kiosk.entity.enums.AdminStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadAdminStatusRequest {

    /*
     * 변경할 계정 상태
     *
     * ACTIVE
     * INACTIVE
     */
    private AdminStatus status;
}