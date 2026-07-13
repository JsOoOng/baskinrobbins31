package com.kiosk.headquarter.dto.security;

import com.kiosk.entity.enums.AdminStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadAdminStatusRequest {

    private AdminStatus status;
}