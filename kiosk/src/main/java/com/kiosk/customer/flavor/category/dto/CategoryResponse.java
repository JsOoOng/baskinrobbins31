package com.kiosk.customer.flavor.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Integer categoryId;    // 카테고리 식별자 [cite: 8]
    private String categoryName;  // 카테고리명 (예: 아이스크림, 음료) [cite: 8]
    private Integer displayOrder; // 노출 순서 [cite: 8]
}