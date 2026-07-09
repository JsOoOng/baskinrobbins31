package com.kiosk.branch.order.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.Order;
import com.kiosk.entity.enums.OrderStatus;
import com.kiosk.entity.enums.OrderType;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class BranchOrderListResponse {


    private Integer orderId;

    private Integer orderNumber;

    private OrderType orderType;

    private OrderStatus orderStatus;

    private Integer totalPrice;

    private LocalDateTime createdAt;


    public static BranchOrderListResponse from(Order order){

        return BranchOrderListResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType())
                .orderStatus(order.getOrderStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .build();

    }

}