package com.kiosk.branch.parttime.dto;

import java.time.LocalDate;

import com.kiosk.entity.enums.StaffStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StaffUpdateRequestDTO {

    private String name;

    private String hp;

    private String email;

    private String address;

    private LocalDate birthDate;

    private LocalDate healthCertEndDate;

    private Integer hourlyWage;
    
    private StaffStatus status;

}