package com.kiosk.headquarter.dto.category;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadCategoryResponseDTO {

    private Integer categoryId;
    private String categoryName;
    private Integer displayOrder;
}