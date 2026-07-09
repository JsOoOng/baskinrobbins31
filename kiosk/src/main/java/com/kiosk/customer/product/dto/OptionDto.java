package com.kiosk.customer.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptionDto {
    private Integer optionId;    // 옵션 식별자 
    private String optionName;   // 옵션명 (예: 초콜릿칩 토핑) 
    private Integer extraPrice;  // 옵션 선택 시 추가 금액 [cite: 13]
}