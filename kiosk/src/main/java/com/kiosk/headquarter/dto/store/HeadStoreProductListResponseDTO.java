package com.kiosk.headquarter.dto.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadStoreProductListResponseDTO {

    private Integer storeProductId;
    private Integer storeId;
    private Integer productId;
    private String productName;
    private String productCategory;
    private Integer storeProductPrice;
    private String storeProductStatus;
}