package com.kiosk.headquarter.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadAdminLoginRequestDTO {

    private String loginId;
    private String password;
}