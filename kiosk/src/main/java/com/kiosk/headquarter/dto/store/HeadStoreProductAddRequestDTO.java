package com.kiosk.headquarter.dto.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadStoreProductAddRequestDTO {

    private Integer productId;
    private Integer storeProductPrice;
    private String storeProductStatus;
}