package com.kiosk.customer.basket.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * [코드 흐름 안내] BasketResponse
 *
 * <p>역할: 장바구니 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@Builder
public class BasketResponse {
    private List<BasketAddRequest> items; // 현재 담긴 상품 목록
    private Integer totalPrice;           // 장바구니 총액
    private Integer totalQuantity;        // 장바구니 총 수량
}