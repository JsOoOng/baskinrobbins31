package com.kiosk.headquarter.dto.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadAdminPasswordRequest {

    private String newPassword;
}