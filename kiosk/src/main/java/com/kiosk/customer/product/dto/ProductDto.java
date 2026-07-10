package com.kiosk.customer.product.dto; // 본인 패키지에 맞게 수정

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class ProductDto {
    private Integer productId;
    private Integer categoryId;
    private String productName;
    private Integer basePrice;
    private String imageUrl;
}