package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadExpenseSummaryResponseDTO {

    private String expenseCategory;
    private Long expenseCount;
    private Long totalExpenseAmount;
}