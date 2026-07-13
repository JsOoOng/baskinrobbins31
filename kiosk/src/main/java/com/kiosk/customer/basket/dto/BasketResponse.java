package com.kiosk.customer.basket.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class BasketResponse {
    private List<BasketAddRequest> items; // 현재 담긴 상품 목록
    private Integer totalPrice;           // 장바구니 총액
    private Integer totalQuantity;        // 장바구니 총 수량
}