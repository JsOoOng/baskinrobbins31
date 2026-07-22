package com.kiosk.branch.status.dto;

import com.kiosk.entity.StoreProduct;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreProductStatusResponse {


    private Integer storeProductId;


    /*
     * 발주용 재고 ID
     */
    private Integer storeInventoryId;


    private Integer productId;


    private String productName;


    /*
     * 최종 품절 상태
     * manualSoldOut || 재고 0
     */
    private Boolean soldOut;


    private Integer currentStock;




    public static StoreProductStatusResponse from(
            StoreProduct sp,
            Integer storeInventoryId,
            Integer currentStock,
            Boolean soldOut
    ){

        return StoreProductStatusResponse.builder()

                .storeProductId(
                        sp.getId()
                )

                .storeInventoryId(
                        storeInventoryId
                )

                .productId(
                        sp.getProduct().getId()
                )

                .productName(
                        sp.getProduct().getProductName()
                )

                .soldOut(
                        soldOut
                )

                .currentStock(
                        currentStock
                )

                .build();

    }

}