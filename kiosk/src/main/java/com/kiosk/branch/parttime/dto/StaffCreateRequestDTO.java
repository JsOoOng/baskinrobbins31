package com.kiosk.branch.parttime.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StaffCreateRequestDTO {

    private String name;

    private String hp;

    private String email;

    private String address;

    private LocalDate birthDate;

    private LocalDate healthCertEndDate;

    private Integer hourlyWage;

}