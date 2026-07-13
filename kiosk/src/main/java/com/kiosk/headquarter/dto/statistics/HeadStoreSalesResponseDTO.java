package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadStoreSalesResponseDTO {

    private Integer storeId;
    private String storeName;
    private Long orderCount;
    private Long totalPaidAmount;
}