package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] HeadStatisticsSummaryResponse
 *
 * <p>역할: 통계 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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