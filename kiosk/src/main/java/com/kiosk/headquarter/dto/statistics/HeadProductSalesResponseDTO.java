package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadProductSalesResponseDTO {

    private Integer productId;
    private String productName;
    private Long totalQuantity;
    private Long totalSalesAmount;
}