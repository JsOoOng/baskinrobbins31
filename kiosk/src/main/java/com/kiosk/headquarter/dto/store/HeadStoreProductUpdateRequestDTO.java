package com.kiosk.headquarter.dto.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadStoreProductUpdateRequestDTO {

    /*
     * 지점별 판매 가격
     */
    private Integer storeProductPrice;

    /*
     * 품절 여부
     */
    private Boolean isSoldOut;
}