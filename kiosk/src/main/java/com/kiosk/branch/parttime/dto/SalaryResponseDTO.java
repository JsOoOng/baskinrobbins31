package com.kiosk.branch.parttime.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalaryResponseDTO {

    private Integer staffId;

    private String staffName;

    private Integer hourlyWage;

    private Double totalHours;

    private Integer weekdayPay;

    private Integer weekendPay;

    private Integer holidayPay;

    private Integer totalSalary;
    
    private Boolean paid;

}