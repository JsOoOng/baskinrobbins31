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



    public static StoreProductStatusResponse from(StoreProduct sp){

        return StoreProductStatusResponse.builder()
                .storeProductId(sp.getId())
                .productId(sp.getProduct().getId())
                .productName(sp.getProduct().getProductName())
                .soldOut(sp.getIsSoldOut())
                .build();
    }

}