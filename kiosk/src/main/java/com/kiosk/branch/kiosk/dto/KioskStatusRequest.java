package com.kiosk.branch.kiosk.dto;


import com.kiosk.entity.enums.KioskStatus;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class KioskStatusRequest {


    private KioskStatus status;


}