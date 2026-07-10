package com.kiosk.headquarter.dto.product;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadProductCreateRequestDTO {

    private Integer categoryId;
    private String productName;
    private String description;
    private Integer basePrice;
    private BigDecimal discountRate;
    private Boolean isDisplay;
    private String imageUrl;
}