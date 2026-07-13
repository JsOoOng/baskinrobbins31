package com.kiosk.branch.statistics.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class SalesPeriodDto {


    private String period;

    private Integer sales;

}