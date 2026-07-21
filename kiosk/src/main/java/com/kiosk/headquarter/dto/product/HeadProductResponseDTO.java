package com.kiosk.headquarter.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadProductResponseDTO {

    private Integer productId;
    private Integer categoryId;
    private String categoryName;
    private String productName;
    private String description;
    private Integer basePrice;
    private Integer marginRate;
    private Integer regularPrice;
    private Integer discountRate;
    private Integer finalPrice;
    private Boolean isDisplay;
    private String imageUrl;
    private LocalDateTime createdAt;
}