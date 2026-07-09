package com.kiosk.headquarter.dto.store;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadStoreProductDetailResponseDTO {

    private Integer storeProductId;
    private Integer storeId;

    private Integer productId;
    private String productName;
    private String description;
    private Integer basePrice;
    private BigDecimal discountRate;
    private Boolean isDisplay;

    private Boolean isSoldOut;
}