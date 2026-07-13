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

    private Integer storeProductPrice;

    /*
     * Product 엔티티의 discountRate가
     * BigDecimal이므로 동일한 타입을 사용합니다.
     */
    private BigDecimal discountRate;

    private Boolean isDisplay;

    private Boolean isSoldOut;
}