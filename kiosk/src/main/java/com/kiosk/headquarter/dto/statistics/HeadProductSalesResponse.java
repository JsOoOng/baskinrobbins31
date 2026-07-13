package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadProductSalesResponse {

    private Integer productId;

    private String productName;

    /*
     * unitPrice × quantity
     *
     * 쿠폰·포인트의 주문 단위 할인은
     * 상품별로 배분하지 않습니다.
     */
    private Long salesAmount;

    private Long salesQuantity;

    private Long orderCount;
}