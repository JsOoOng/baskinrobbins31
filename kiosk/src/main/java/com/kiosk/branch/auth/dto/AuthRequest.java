package com.kiosk.branch.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest {

    private String loginId;

    private String password;

}