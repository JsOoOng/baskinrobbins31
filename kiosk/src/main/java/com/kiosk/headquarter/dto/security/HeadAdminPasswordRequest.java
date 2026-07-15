package com.kiosk.headquarter.dto.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadAdminPasswordRequest {

    /*
     * 초기화할 새 비밀번호
     */
    private String newPassword;
}