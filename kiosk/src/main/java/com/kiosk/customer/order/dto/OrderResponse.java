package com.kiosk.customer.order.dto;

import java.util.List;

import com.kiosk.entity.OrderItem;

import lombok.Data;

@Data
public class OrderResponse {
    private int orderId;
    private int totalPrice;
    // 엔티티 대신 전용 DTO 리스트 사용
    private List<OrderItemDTO> orderItems; 
}