package com.kiosk.branch.parttime.dto;

import com.kiosk.entity.enums.StaffStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor
public class StaffListResponseDTO {


    private Integer staffId;


    private String name;


    private String hp;


    private String email;


    private String address;


    private String birthDate;


    private String healthCertEndDate;


    private Integer hourlyWage;


    private StaffStatus status;


}