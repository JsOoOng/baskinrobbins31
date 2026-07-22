package com.kiosk.headquarter.dto.inventory;

import java.time.LocalDateTime;

import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.AutoRestockMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadInventoryResponse {

    private Integer storeInventoryId;

    private Integer storeId;

    private String storeName;

    private Integer itemId;

    private Integer productId;

    private String productName;

    private String unit;

    private Integer unitPrice;

    private Integer currentStock;

    private Integer minStock;

    private Integer targetStock;

    private Boolean autoRestockEnabled;

    private AutoRestockMode restockMode;

    private LocalDateTime lastUpdated;

    /*
     * StoreInventory 엔티티를
     * 본사 재고 응답 DTO로 변환합니다.
     */
    public static HeadInventoryResponse from(
            StoreInventory inventory
    ) {

        return HeadInventoryResponse.builder()
                .storeInventoryId(
                        inventory.getId()
                )
                .storeId(
                        inventory.getStore()
                                .getId()
                )
                .storeName(
                        inventory.getStore()
                                .getStoreName()
                )
                .itemId(
                        inventory.getItem()
                                .getId()
                )
                .productId(
                        inventory.getItem()
                                .getProduct()
                                .getId()
                )
                .productName(
                        inventory.getItem()
                                .getProduct()
                                .getProductName()
                )
                .unit(
                        inventory.getItem()
                                .getUnit()
                )
                .unitPrice(
                        inventory.getItem()
                                .getUnitPrice()
                )
                .currentStock(
                        inventory.getCurrentStock()
                )
                .minStock(
                        inventory.getMinStock()
                )
                .targetStock(
                        inventory.getTargetStock()
                )
                .autoRestockEnabled(
                        inventory.getAutoRestockEnabled()
                )
                .restockMode(
                        inventory.getRestockMode()
                )
                .lastUpdated(
                        inventory.getLastUpdated()
                )
                .build();
    }
}