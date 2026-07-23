package com.kiosk.headquarter.dto.statistics;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] HeadFlavorSalesResponse
 *
 * <p>역할: 아이스크림 맛 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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