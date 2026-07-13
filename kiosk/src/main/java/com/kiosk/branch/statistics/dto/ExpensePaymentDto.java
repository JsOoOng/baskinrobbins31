package com.kiosk.branch.statistics.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class ExpensePaymentDto {


    private String paymentMethod;

    private Integer amount;

}