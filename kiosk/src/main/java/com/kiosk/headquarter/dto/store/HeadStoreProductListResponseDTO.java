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

    /*
     * 본사 기본 가격
     */
    private Integer basePrice;

    /*
     * 해당 지점 판매 가격
     */
    private Integer storeProductPrice;

    private Boolean isSoldOut;
}