package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadSalesSummaryResponseDTO {

    private Long orderCount;
    private Long totalOrderAmount;
    private Long totalPaidAmount;
    private Long totalCouponDiscount;
    private Long totalPointUsed;
    private Long totalExpenseAmount;
    private Long netProfit;
}