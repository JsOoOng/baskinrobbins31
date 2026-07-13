package com.kiosk.headquarter.dto.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadCategoryCreateRequestDTO {

    private String categoryName;
    private Integer displayOrder;
}