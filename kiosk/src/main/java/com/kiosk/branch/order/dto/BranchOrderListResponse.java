package com.kiosk.branch.order.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kiosk.entity.Order;
import com.kiosk.entity.enums.OrderStatus;
import com.kiosk.entity.enums.OrderType;
import com.kiosk.entity.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] BranchOrderListResponse
 *
 * <p>역할: 주문 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BranchOrderListResponse {

    private Integer orderId;

    private Integer orderNumber;

    private OrderType orderType;

    private OrderStatus orderStatus;

    // Payment 정보
    private Integer finalAmount;

    private PaymentStatus paymentStatus;

    private LocalDateTime createdAt;

    
    public static BranchOrderListResponse from(Order order) {

        return BranchOrderListResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())

                .finalAmount(
                        order.getPayment() != null
                        ? order.getPayment().getFinalAmount()
                        : null
                )

                .paymentStatus(
                        order.getPayment() != null
                        ? order.getPayment().getPaymentStatus()
                        : null
                )

                .createdAt(order.getCreatedAt())

                .build();
    }

}