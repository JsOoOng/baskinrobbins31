package com.kiosk.headquarter.dto.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadCategoryCreateRequestDTO {

    private String categoryName;
    private Integer displayOrder;

    // 값을 보내지 않으면 Service에서 true 처리
    private Boolean active;
}