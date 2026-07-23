package com.kiosk.customer.product.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] ProductOptionDto
 *
 * <p>역할: 상품 옵션 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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