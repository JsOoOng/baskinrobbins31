package com.kiosk.headquarter.dto.admin;

import com.kiosk.entity.enums.AdminStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadAdminStatusUpdateRequestDTO {

    private AdminStatus status;
}