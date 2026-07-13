package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadStoreSalesResponse {

    private Integer storeId;

    private String storeName;

    private Long totalSales;

    private Long orderCount;

    private Long salesQuantity;
}