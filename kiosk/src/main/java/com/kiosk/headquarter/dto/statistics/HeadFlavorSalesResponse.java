package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadFlavorSalesResponse {

    /*
     * 맛 ID
     */
    private Integer flavorId;

    /*
     * 맛 이름
     */
    private String flavorName;

    /*
     * 선택된 맛 수량
     */
    private Long salesQuantity;

    /*
     * 해당 맛이 포함된 주문 수
     */
    private Long orderCount;

    /*
     * 맛에 배분된 판매 금액
     */
    private Long salesAmount;
}