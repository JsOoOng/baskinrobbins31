package com.kiosk.customer.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateRequest {
    private Integer storeId;       // 지점 ID
    private Integer kioskId;       // 키오스크 기기 ID
    private Integer userId;        // 적립 회원 ID (비회원이면 null)
    private String orderType;      // "HERE" 또는 "TOGO"
    private Integer dryIceMins;    // 드라이아이스 시간 (포장일 경우)
}