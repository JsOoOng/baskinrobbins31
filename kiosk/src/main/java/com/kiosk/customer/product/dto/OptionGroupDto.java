package com.kiosk.customer.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * [코드 흐름 안내] OptionGroupDto
 *
 * <p>역할: 상품·메뉴 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OptionGroupDto {
    private String optionType;          // 옵션 종류 구분 (예: SIZE, TOPPING, SPOON) 
    private List<OptionDto> options;    // 해당 그룹에 속하는 상세 옵션 리스트
}