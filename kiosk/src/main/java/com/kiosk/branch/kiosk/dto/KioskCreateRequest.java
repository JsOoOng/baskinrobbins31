package com.kiosk.branch.kiosk.dto;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class KioskCreateRequest {


    private Integer storeId;


    private Integer kioskNumber;


    private String deviceSerial;


}