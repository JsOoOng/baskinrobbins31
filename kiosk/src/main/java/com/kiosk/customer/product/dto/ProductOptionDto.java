package com.kiosk.customer.product.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class ProductOptionDto {
    private Integer optionId;
    private Integer productId;
    private String optionType; // 'CONTAINER', 'SIZE', 'TOPPING' 등
    private String optionName; // '초콜릿칩 토핑', '스푼 3개' 등
    private Integer extraPrice;
    private Integer maxFlavorCount; // 아이스크림 사이즈일 경우 제한 개수
}