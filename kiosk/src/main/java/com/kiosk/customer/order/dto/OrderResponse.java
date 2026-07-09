package com.kiosk.customer.order.dto;

import java.util.List;

import com.kiosk.entity.OrderItem;

import lombok.Data;

@Data
public class OrderResponse {
    private int orderId;
    private int totalPrice;
    // 주문 항목들을 담을 리스트 (OrderItem DTO 활용)
    private List<OrderItem> orderItems; 
}
