package com.kiosk.branch.expense.dto;

import com.kiosk.entity.enums.ExpenseCategory;
import com.kiosk.entity.enums.ExpensePaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ExpenseHistoryResponseDTO {

    /*
     * 지출 내용
     */
    private String description;

    /*
     * 지출 분류
     */
    private ExpenseCategory expenseCategory;

    /*
     * 결제 방식
     */
    private ExpensePaymentMethod paymentMethod;

}