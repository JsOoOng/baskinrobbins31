package com.kiosk.customer.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] CategoryResponse
 *
 * <p>역할: 상품 카테고리 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Integer categoryId;    // 카테고리 식별자 [cite: 8]
    private String categoryName;  // 카테고리명 (예: 아이스크림, 음료) [cite: 8]
    private Integer displayOrder; // 노출 순서 [cite: 8]
}