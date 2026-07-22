package com.kiosk.common.inventory;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.AutoRestockMode;
import com.kiosk.headquarter.repository.HeadStoreInventoryMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoRestockService {

    private final HeadStoreInventoryMapper
            headStoreInventoryMapper;

    /*
     * 정해진 시간에 DAILY 또는 BOTH 재고를
     * 목표 재고까지 자동 보충합니다.
     */
    @Transactional
    public void processDailyRestock() {

        List<StoreInventory> inventories =
                headStoreInventoryMapper
                        .findByAutoRestockEnabledTrueAndRestockModeIn(
                                List.of(
                                        AutoRestockMode.DAILY,
                                        AutoRestockMode.BOTH
                                )
                        );

        int processedCount = 0;
        int totalRestockQuantity = 0;

        for (StoreInventory inventory : inventories) {

            if (!inventory.needsDailyRestock()) {
                continue;
            }

            Integer restockQuantity =
                    inventory.autoRestock();

            if (
                    restockQuantity == null ||
                    restockQuantity <= 0
            ) {
                continue;
            }

            processedCount++;
            totalRestockQuantity +=
                    restockQuantity;

            log.info(
                    "정기 자동 재고 보충: "
                            + "storeInventoryId={}, "
                            + "storeId={}, "
                            + "itemId={}, "
                            + "restockQuantity={}, "
                            + "currentStock={}",
                    inventory.getId(),
                    inventory.getStore().getId(),
                    inventory.getItem().getId(),
                    restockQuantity,
                    inventory.getCurrentStock()
            );
        }

        log.info(
                "정기 자동 재고 보충 완료: "
                        + "processedCount={}, "
                        + "totalRestockQuantity={}",
                processedCount,
                totalRestockQuantity
        );
    }

    /*
     * 상품 판매 직후 임계 재고 검사
     */
    @Transactional
    public Integer processThresholdRestock(
            StoreInventory inventory
    ) {

        if (
                inventory == null ||
                !inventory.needsThresholdRestock()
        ) {
            return 0;
        }

        Integer restockQuantity =
                inventory.autoRestock();

        if (
                restockQuantity == null ||
                restockQuantity <= 0
        ) {
            return 0;
        }

        log.info(
                "임계 재고 자동 보충: "
                        + "storeInventoryId={}, "
                        + "restockQuantity={}, "
                        + "currentStock={}",
                inventory.getId(),
                restockQuantity,
                inventory.getCurrentStock()
        );

        return restockQuantity;
    }
}