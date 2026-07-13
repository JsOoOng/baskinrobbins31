package com.kiosk.branch.statistics.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ExpensePeriodDto {


    private String period;

    private Integer amount;

}