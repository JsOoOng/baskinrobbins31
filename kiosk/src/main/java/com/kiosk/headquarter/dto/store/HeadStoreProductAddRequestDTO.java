package com.kiosk.headquarter.dto.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadStoreProductAddRequestDTO {

    /*
     * 본사 상품 ID
     */
    private Integer productId;

    /*
     * 지점 판매 가격
     *
     * null이면 본사 기본 가격을 사용합니다.
     */
    private Integer storeProductPrice;

    /*
     * 품절 여부
     */
    private Boolean isSoldOut;
}