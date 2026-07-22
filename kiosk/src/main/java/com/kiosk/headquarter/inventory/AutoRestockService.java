package com.kiosk.headquarter.inventory;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.AutoRestockMode;
import com.kiosk.headquarter.repository.HeadStoreInventoryMapper;
import com.kiosk.headquarter.service.HeadNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoRestockService {

    private final HeadStoreInventoryMapper
            headStoreInventoryMapper;

    private final HeadNotificationService
            headNotificationService;

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

        for (
                StoreInventory inventory
                : inventories
        ) {

            if (
                    inventory == null ||
                    !inventory.needsDailyRestock()
            ) {
                continue;
            }

            Integer restockQuantity =
                    executeAutoRestock(
                            inventory
                    );

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
     * THRESHOLD 또는 BOTH 재고를
     * 전체 검사합니다.
     *
     * 현재 재고가 최소 재고 이하이면
     * 목표 재고까지 보충합니다.
     *
     * 테스트 및 누락 복구용 전체 검사입니다.
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

        for (
                StoreInventory inventory
                : inventories
        ) {

            if (
                    inventory == null ||
                    !inventory.needsThresholdRestock()
            ) {
                continue;
            }

            Integer restockQuantity =
                    executeAutoRestock(
                            inventory
                    );

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
     *
     * 자동 보충됨
     * → 실제 보충 수량 반환
     *
     * 보충 조건에 해당하지 않음
     * → 0 반환
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
                executeAutoRestock(
                        inventory
                );

        if (restockQuantity > 0) {
            log.info(
                    "주문 후 임계 자동 재고 보충: "
                            + "storeInventoryId={}, "
                            + "restockQuantity={}, "
                            + "currentStock={}",
                    inventory.getId(),
                    restockQuantity,
                    inventory.getCurrentStock()
            );
        }

        return restockQuantity;
    }

    /*
     * 실제 자동 보충 공통 처리
     *
     * 정기 보충, 임계 보충,
     * 주문 직후 보충이 모두 이 메서드를 사용합니다.
     */
    private Integer executeAutoRestock(
            StoreInventory inventory
    ) {

        if (inventory == null) {
            return 0;
        }

        Integer storeInventoryId =
                inventory.getId();

        String storeName =
                getStoreName(
                        inventory
                );

        String productName =
                getProductName(
                        inventory
                );

        Integer previousStock =
                inventory.getCurrentStock();

        try {
            /*
             * 목표 수량까지 실제 재고 보충
             */
            Integer restockedQuantity =
                    inventory.autoRestock();

            if (
                    restockedQuantity == null ||
                    restockedQuantity <= 0
            ) {
                return 0;
            }

            /*
             * 즉시 DB에 반영하여
             * SQL 오류도 현재 try 블록에서 확인합니다.
             */
            headStoreInventoryMapper
                    .saveAndFlush(
                            inventory
                    );

            /*
             * 자동 보충 성공 알림 생성
             */
            headNotificationService
                    .createAutoRestockSuccessNotification(
                            storeInventoryId,
                            storeName,
                            productName,
                            restockedQuantity,
                            inventory.getCurrentStock()
                    );

            log.info(
                    "자동 재고 보충 성공: "
                            + "storeInventoryId={}, "
                            + "previousStock={}, "
                            + "restockedQuantity={}, "
                            + "currentStock={}",
                    storeInventoryId,
                    previousStock,
                    restockedQuantity,
                    inventory.getCurrentStock()
            );

            return restockedQuantity;

        } catch (Exception exception) {

            log.error(
                    "자동 재고 보충 실패: "
                            + "storeInventoryId={}, "
                            + "storeName={}, "
                            + "productName={}, "
                            + "reason={}",
                    storeInventoryId,
                    storeName,
                    productName,
                    extractFailureReason(
                            exception
                    ),
                    exception
            );

            /*
             * 재고 처리 트랜잭션이 롤백되어도
             * 실패 알림은 별도 트랜잭션으로 저장되어야 합니다.
             *
             * HeadNotificationService의
             * createAutoRestockFailureNotification()에
             * REQUIRES_NEW가 있어야 합니다.
             */
            try {
                headNotificationService
                        .createAutoRestockFailureNotification(
                                storeInventoryId,
                                storeName,
                                productName,
                                extractFailureReason(
                                        exception
                                )
                        );

            } catch (
                    Exception notificationException
            ) {
                log.error(
                        "자동 재고 보충 실패 알림 저장 실패: "
                                + "storeInventoryId={}",
                        storeInventoryId,
                        notificationException
                );
            }

            /*
             * 실패를 숨기지 않고 다시 발생시켜
             * 잘못된 재고 변경이 커밋되지 않도록 합니다.
             */
            if (
                    exception
                    instanceof RuntimeException
                    runtimeException
            ) {
                throw runtimeException;
            }

            throw new IllegalStateException(
                    "자동 재고 보충 처리에 실패했습니다.",
                    exception
            );
        }
    }

    /*
     * 지점명 조회
     */
    private String getStoreName(
            StoreInventory inventory
    ) {

        if (
                inventory == null ||
                inventory.getStore() == null ||
                inventory.getStore()
                        .getStoreName() == null ||
                inventory.getStore()
                        .getStoreName()
                        .isBlank()
        ) {
            return "지점명 없음";
        }

        return inventory.getStore()
                .getStoreName()
                .trim();
    }

    /*
     * 상품명 조회
     *
     * 상품명이 없으면 재고 품목명을 사용합니다.
     */
    private String getProductName(
            StoreInventory inventory
    ) {

        if (
                inventory == null ||
                inventory.getItem() == null
        ) {
            return "상품명 없음";
        }

        InventoryItem item =
                inventory.getItem();

        if (
                item.getProduct() != null &&
                item.getProduct()
                        .getProductName() != null &&
                !item.getProduct()
                        .getProductName()
                        .isBlank()
        ) {
            return item.getProduct()
                    .getProductName()
                    .trim();
        }

        if (
                item.getItemName() != null &&
                !item.getItemName()
                        .isBlank()
        ) {
            return item.getItemName()
                    .trim();
        }

        return "상품명 없음";
    }

    /*
     * 알림에 기록할 실패 원인 정리
     */
    private String extractFailureReason(
            Exception exception
    ) {

        if (exception == null) {
            return "알 수 없는 오류";
        }

        String exceptionMessage =
                exception.getMessage();

        if (
                exceptionMessage == null ||
                exceptionMessage.isBlank()
        ) {
            return exception
                    .getClass()
                    .getSimpleName();
        }

        String normalizedMessage =
                exceptionMessage.trim();

        /*
         * DB 오류 메시지가 지나치게 길어지는 것을 방지
         */
        if (
                normalizedMessage.length() >
                150
        ) {
            return normalizedMessage.substring(
                    0,
                    150
            );
        }

        return normalizedMessage;
    }
}