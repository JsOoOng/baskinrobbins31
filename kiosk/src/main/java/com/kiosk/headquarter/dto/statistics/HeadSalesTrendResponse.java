package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadSalesTrendResponse {

    /*
     * 일별: 2026-07-13
     * 주별: 2026-07-13
     * 월별: 2026-07
     * 연별: 2026
     */
    private String periodLabel;

    private Long totalSales;

    private Long orderCount;

    private Long salesQuantity;
}