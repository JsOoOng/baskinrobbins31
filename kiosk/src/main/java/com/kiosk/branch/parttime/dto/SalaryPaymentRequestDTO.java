package com.kiosk.branch.parttime.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class SalaryPaymentRequestDTO {


    private Integer employeeId;


    private String paymentMethod;


    private String description;


    private Integer year;


    private Integer month;

}