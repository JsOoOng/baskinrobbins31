package com.kiosk.customer.order.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderItemDTO {
    private Integer id;
    private Integer productId;
    private String productName; // 상품명
    private Integer quantity;   // 수량
    private Integer unitPrice;  // 단가
    private List<String> flavors; // 맛 리스트
}