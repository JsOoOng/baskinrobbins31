package com.kiosk.branch.statistics.dto;

import java.time.LocalDate;

import com.kiosk.entity.enums.ExpenseCategory;
import com.kiosk.entity.enums.ExpensePaymentMethod;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class ExpenseDetailDto {


    private Integer expenseId;


    private LocalDate expenseDate;


    private String description;


    private Integer amount;


    private ExpenseCategory expenseCategory;


    private ExpensePaymentMethod paymentMethod;


}