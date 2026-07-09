package com.kiosk.headquarter.dto.store;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadStoreProductListResponseDTO {

    private Integer storeProductId;
    private Integer storeId;
    private Integer productId;
    private String productName;
    private Integer basePrice;
    private Boolean isSoldOut;
}