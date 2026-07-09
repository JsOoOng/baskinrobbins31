package com.kiosk.customer.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupDto {
    private String optionType;          // 옵션 종류 구분 (예: SIZE, TOPPING, SPOON) 
    private List<OptionDto> options;    // 해당 그룹에 속하는 상세 옵션 리스트
}