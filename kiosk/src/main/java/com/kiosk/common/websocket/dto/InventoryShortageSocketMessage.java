package com.kiosk.common.websocket.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.InventoryShortageAlert;
import com.kiosk.entity.Store;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryShortageSocketMessage {

    /*
     * 알림 유형
     *
     * INVENTORY_SHORTAGE
     */
    private String notificationType;

    /*
     * 알림 발생 주체
     *
     * SYSTEM
     * → 시스템 자동 알림
     *
     * HEADQUARTER
     * → 본사 관리자가 직접 보낸 알림
     */
    private String source;

    private String title;
    private String message;
    private String routeName;

    /*
     * 아래 ID는 화면에 출력하지 않습니다.
     *
     * API 처리와 화면 이동에만 사용합니다.
     */
    private Integer alertId;
    private Integer storeInventoryId;
    private Integer storeId;
    private Integer itemId;

    private String storeName;
    private String itemName;

    private Integer currentStock;
    private Integer minStock;
    private Integer shortageQuantity;

    private String alertStatus;
    private LocalDateTime detectedAt;

    /*
     * 본사에 자동으로 보내는 부족 알림
     */
    public static InventoryShortageSocketMessage
            forHead(
                    InventoryShortageAlert alert
            ) {

        validateAlert(alert);

        String storeName =
                getStoreName(
                        alert.getStore()
                );

        String title;

        if (
                "RESOLVED".equals(
                        alert.getAlertStatus().name()
                )
        ) {
            title =
                    storeName
                    + "의 재고 부족이 해소되었습니다.";

        } else {
            title =
                    storeName
                    + "에 부족한 재고가 있습니다.";
        }

        return baseBuilder(alert)
                .source("SYSTEM")
                .title(title)
                .message(
                        "재고 현황에서 부족 품목을 확인해주세요."
                )
                .routeName(
                        "head-inventory"
                )
                .build();
    }

    /*
     * 시스템이 지점에 자동으로 보내는 자체 알림
     */
    public static InventoryShortageSocketMessage
            forStoreSelf(
                    InventoryShortageAlert alert
            ) {

        validateAlert(alert);

        return baseBuilder(alert)
                .source("SYSTEM")
                .title(
                        "부족한 재고가 있습니다."
                )
                .message(
                        "재고 관리 화면에서 부족 품목을 확인해주세요."
                )
                .routeName(
                        "branch-inventory"
                )
                .build();
    }

    /*
     * 본사 관리자가 버튼을 눌러
     * 지점에 직접 보내는 알림
     */
    public static InventoryShortageSocketMessage
            forStoreHeadReminder(
                    InventoryShortageAlert alert
            ) {

        validateAlert(alert);

        return baseBuilder(alert)
                .source("HEADQUARTER")
                .title(
                        "본사에서 재고 부족 알림을 보냈습니다."
                )
                .message(
                        "재고 관리 화면에서 부족 품목을 확인해주세요."
                )
                .routeName(
                        "branch-inventory"
                )
                .build();
    }

    private static InventoryShortageSocketMessageBuilder
            baseBuilder(
                    InventoryShortageAlert alert
            ) {

        Store store =
                alert.getStore();

        InventoryItem item =
                alert.getItem();

        return InventoryShortageSocketMessage
                .builder()
                .notificationType(
                        "INVENTORY_SHORTAGE"
                )
                .alertId(
                        alert.getId()
                )
                .storeInventoryId(
                        alert.getStoreInventory() == null
                                ? null
                                : alert.getStoreInventory()
                                        .getId()
                )
                .storeId(
                        store == null
                                ? null
                                : store.getId()
                )
                .itemId(
                        item == null
                                ? null
                                : item.getId()
                )
                .storeName(
                        getStoreName(store)
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
                .alertStatus(
                        alert.getAlertStatus() == null
                                ? null
                                : alert.getAlertStatus()
                                        .name()
                )
                .detectedAt(
                        alert.getDetectedAt()
                );
    }

    private static void validateAlert(
            InventoryShortageAlert alert
    ) {

        if (alert == null) {
            throw new IllegalArgumentException(
                    "재고 부족 알람 정보가 없습니다."
            );
        }
    }

    private static String getStoreName(
            Store store
    ) {

        if (
                store == null ||
                store.getStoreName() == null ||
                store.getStoreName().isBlank()
        ) {
            return "지점";
        }

        return store.getStoreName()
                .trim();
    }

    private static String getItemName(
            InventoryItem item
    ) {

        if (
                item == null ||
                item.getItemName() == null ||
                item.getItemName().isBlank()
        ) {
            return "재고 품목";
        }

        return item.getItemName()
                .trim();
    }
}