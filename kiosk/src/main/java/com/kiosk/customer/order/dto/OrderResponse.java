package com.kiosk.customer.order.dto;

import java.util.List;

import com.kiosk.entity.OrderItem;

import lombok.Data;

/**
 * [코드 흐름 안내] OrderResponse
 *
 * <p>역할: 주문 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Data
public class OrderResponse {
    private int orderId;
    private int orderNumber;
    private int totalPrice;
    // 엔티티 대신 전용 DTO 리스트 사용
    private List<OrderItemDTO> orderItems; 
}

