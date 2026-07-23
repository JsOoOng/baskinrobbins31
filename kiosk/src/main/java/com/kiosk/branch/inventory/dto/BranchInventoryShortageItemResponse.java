package com.kiosk.branch.inventory.dto;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.InventoryShortageAlert;
import com.kiosk.entity.StoreInventory;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchInventoryShortageItemResponse {

    /*
     * 화면에는 출력하지 않고
     * API 처리에만 사용하는 번호입니다.
     */
    private Integer alertId;

    private Integer storeInventoryId;
    private Integer itemId;

    /*
     * 화면에 표시할 재고 품목명
     */
    private String itemName;

    private Integer currentStock;
    private Integer minStock;
    private Integer shortageQuantity;

    public static BranchInventoryShortageItemResponse from(
            InventoryShortageAlert alert
    ) {

        if (alert == null) {
            throw new IllegalArgumentException(
                    "재고 부족 알람 정보가 없습니다."
            );
        }

        StoreInventory storeInventory =
                alert.getStoreInventory();

        InventoryItem item =
                alert.getItem();

        return BranchInventoryShortageItemResponse
                .builder()
                .alertId(
                        alert.getId()
                )
                .storeInventoryId(
                        storeInventory == null
                                ? null
                                : storeInventory.getId()
                )
                .itemId(
                        item == null
                                ? null
                                : item.getId()
                )
                .itemName(
                        getItemName(item)
                )
                .currentStock(
                        alert.getCurrentStockSnapshot()
                )
                .minStock(
                        alert.getMinStockSnapshot()
                )
                .shortageQuantity(
                        alert.getShortageQuantity()
                )
                .build();
    }

    private static String getItemName(
            InventoryItem item
    ) {

        if (
                item == null ||
                item.getItemName() == null ||
                item.getItemName().isBlank()
        ) {
            return "재고 품목명 없음";
        }

        return item.getItemName()
                .trim();
    }
}