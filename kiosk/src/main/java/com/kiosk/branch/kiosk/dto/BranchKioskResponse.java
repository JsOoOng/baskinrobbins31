package com.kiosk.branch.kiosk.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.Kiosk;
import com.kiosk.entity.enums.KioskStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchKioskResponse {

    private Integer kioskId;

    private Integer kioskNumber;

    private String deviceSerial;

    private KioskStatus kioskStatus;

    private LocalDateTime createdAt;


    public static BranchKioskResponse from(Kiosk kiosk){

        return BranchKioskResponse.builder()
                .kioskId(kiosk.getId())
                .kioskNumber(kiosk.getKioskNumber())
                .deviceSerial(kiosk.getDeviceSerial())
                .kioskStatus(kiosk.getKioskStatus())
                .createdAt(kiosk.getCreatedAt())
                .build();

    }

}