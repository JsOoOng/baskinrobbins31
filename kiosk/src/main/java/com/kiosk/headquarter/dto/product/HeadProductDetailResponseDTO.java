package com.kiosk.headquarter.dto.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadProductDetailResponseDTO {

    private Integer productId;
    private String productName;
    private Integer productPrice;
    private String productCategory;
    private String productStatus;
    private String productDescription;
}