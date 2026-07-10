package com.kiosk.customer.category.dto; // 본인 패키지에 맞게 수정

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class CategoryDto {
    private Integer categoryId;
    private String categoryName;
}