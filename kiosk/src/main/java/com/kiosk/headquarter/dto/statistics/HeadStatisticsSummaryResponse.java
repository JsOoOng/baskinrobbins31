package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadStatisticsSummaryResponse {

    /*
     * 실제 결제 완료 금액
     */
    private Long totalSales;

    /*
     * 결제 완료 주문 수
     */
    private Long orderCount;

    /*
     * 판매 상품 총수량
     */
    private Long salesQuantity;

    /*
     * 주문 1건당 평균 결제 금액
     */
    private Long averageOrderAmount;
}