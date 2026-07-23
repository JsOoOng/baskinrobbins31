package com.kiosk.headquarter.dto.inventoryalert;

import java.time.LocalDateTime;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.InventoryShortageAlert;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.InventoryShortageAlertStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] HeadInventoryShortageSocketResponse
 *
 * <p>역할: 재고 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
@AllArgsConstructor
public class HeadInventoryShortageSocketResponse {

    /*
     * 부족 알람 번호
     */
    private Integer alertId;

    /*
     * 지점 재고 번호
     */
    private Integer storeInventoryId;

    /*
     * 지점 정보
     */
    private Integer storeId;
    private String storeName;

    /*
     * 재고 품목 정보
     */
    private Integer itemId;
    private String itemName;

    /*
     * 현재 재고와 최소 재고
     */
    private Integer currentStock;
    private Integer minStock;

    /*
     * 부족 수량
     *
     * minStock - currentStock
     */
    private Integer shortageQuantity;

    /*
     * 알람 상태
     */
    private InventoryShortageAlertStatus
            alertStatus;

    /*
     * 최초 부족 감지 시각
     */
    private LocalDateTime detectedAt;

    /*
     * 엔티티를 WebSocket 응답으로 변환
     */
    public static HeadInventoryShortageSocketResponse
            from(
                    InventoryShortageAlert alert
            ) {

        if (alert == null) {
            throw new IllegalArgumentException(
                    "재고 부족 알람 정보가 없습니다."
            );
        }

        StoreInventory storeInventory =
                alert.getStoreInventory();

        Store store =
                alert.getStore();

        InventoryItem item =
                alert.getItem();

        return HeadInventoryShortageSocketResponse
                .builder()
                .alertId(
                        alert.getId()
                )
                .storeInventoryId(
                        storeInventory == null
                                ? null
                                : storeInventory.getId()
                )
                .storeId(
                        store == null
                                ? null
                                : store.getId()
                )
                .storeName(
                        getStoreName(
                                store
                        )
                )
                .itemId(
                        item == null
                                ? null
                                : item.getId()
                )
                .itemName(
                        getItemName(
                                item
                        )
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
                        alert.getAlertStatus()
                )
                .detectedAt(
                        alert.getDetectedAt()
                )
                .build();
    }

    private static String getStoreName(
            Store store
    ) {

        if (
                store == null ||
                store.getStoreName() == null ||
                store.getStoreName().isBlank()
        ) {
            return "지점명 없음";
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
            return "재고 품목명 없음";
        }

        return item.getItemName()
                .trim();
    }
}