package com.kiosk.customer.flavor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlavorResponse {
    private Integer flavorId;     // 맛 식별자 [cite: 14]
    private String flavorName;   // 맛 이름 (예: 민트초코, 엄마는 외계인) [cite: 14]
    private Boolean isSoldOut;   // 현재 지점 내 품절 여부 (STORE_FLAVORS 테이블 연동) [cite: 17, 18]
}