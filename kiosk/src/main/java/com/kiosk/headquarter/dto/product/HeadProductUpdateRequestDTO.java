package com.kiosk.headquarter.dto.product;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadProductUpdateRequestDTO {

    private String productName;
    private Integer productPrice;
    private String productCategory;
    private String productStatus;
    private String productDescription;
}