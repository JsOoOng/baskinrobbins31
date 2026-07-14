package com.kiosk.branch.status.dto;

import com.kiosk.entity.StoreProduct;

import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class StoreProductStatusResponse {


    private Integer storeProductId;
    
    private Integer productId;

    private String productName;

    private Boolean soldOut;

    private Integer currentStock;

    public static StoreProductStatusResponse from(
            StoreProduct sp,
            Integer currentStock
    ){

        return StoreProductStatusResponse.builder()
                .storeProductId(sp.getId())
                .productId(sp.getProduct().getId())
                .productName(sp.getProduct().getProductName())
                .soldOut(sp.getIsSoldOut())
                .currentStock(currentStock)
                .build();

    }

}