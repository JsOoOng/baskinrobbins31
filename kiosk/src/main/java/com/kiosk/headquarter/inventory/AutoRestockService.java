package com.kiosk.headquarter.inventory;

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
     * DAILY 또는 BOTH 재고를 검사합니다.
     *
     * 현재 재고가 목표 재고보다 적으면
     * 목표 재고까지 보충합니다.
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

            if (restockQuantity <= 0) {
                continue;
            }

            processedCount++;
            totalRestockQuantity +=
                    restockQuantity;

            log.info(
                    "정기 자동 재고 보충: "
                            + "storeInventoryId={}, "
                            + "restockQuantity={}, "
                            + "currentStock={}",
                    inventory.getId(),
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
     * THRESHOLD 또는 BOTH 재고를 전체 검사합니다.
     *
     * 현재 재고가 최소 재고 이하이면
     * 목표 재고까지 보충합니다.
     */
    @Transactional
    public void processThresholdRestockSweep() {

        List<StoreInventory> inventories =
                headStoreInventoryMapper
                        .findByAutoRestockEnabledTrueAndRestockModeIn(
                                List.of(
                                        AutoRestockMode.THRESHOLD,
                                        AutoRestockMode.BOTH
                                )
                        );

        int processedCount = 0;
        int totalRestockQuantity = 0;

        for (StoreInventory inventory : inventories) {

            if (!inventory.needsThresholdRestock()) {
                continue;
            }

            Integer restockQuantity =
                    inventory.autoRestock();

            if (restockQuantity <= 0) {
                continue;
            }

            processedCount++;
            totalRestockQuantity +=
                    restockQuantity;

            log.info(
                    "임계 자동 재고 보충: "
                            + "storeInventoryId={}, "
                            + "currentStock={}, "
                            + "minStock={}, "
                            + "targetStock={}, "
                            + "restockQuantity={}",
                    inventory.getId(),
                    inventory.getCurrentStock(),
                    inventory.getMinStock(),
                    inventory.getTargetStock(),
                    restockQuantity
            );
        }

        log.info(
                "임계 자동 재고 보충 검사 완료: "
                        + "processedCount={}, "
                        + "totalRestockQuantity={}",
                processedCount,
                totalRestockQuantity
        );
    }

    /*
     * 주문 직후 특정 재고 한 건을 검사합니다.
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

        log.info(
                "주문 후 임계 자동 재고 보충: "
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