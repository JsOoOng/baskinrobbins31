package com.kiosk.headquarter.dto.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadCategoryUpdateRequestDTO {

    private String categoryName;
    private Integer displayOrder;
}