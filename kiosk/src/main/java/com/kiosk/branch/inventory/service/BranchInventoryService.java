package com.kiosk.branch.inventory.service;

import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.inventory.repository.BranchInventoryMapper;
import com.kiosk.branch.status.reopsitory.StoreProductMapper;
import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Order;
import com.kiosk.entity.OrderItem;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.StoreProduct;
import com.kiosk.headquarter.inventory.AutoRestockService;
import com.kiosk.headquarter.service.HeadNotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BranchInventoryService {

    private final BranchInventoryMapper
            inventoryMapper;

    private final AutoRestockService
            autoRestockService;

    private final StoreProductMapper
            storeProductMapper;

    private final HeadNotificationService
            headNotificationService;

    /*
     * 주문 완료 후 재고 차감
     */
    public void decrease(
            Order order
    ) {

        validateOrder(order);

        if (
                order.getOrderItems() == null ||
                order.getOrderItems().isEmpty()
        ) {
            return;
        }

        for (
                OrderItem orderItem
                : order.getOrderItems()
        ) {

            if (
                    orderItem == null ||
                    orderItem.getProduct() == null
            ) {
                continue;
            }

            Integer orderQuantity =
                    orderItem.getQuantity();

            if (
                    orderQuantity == null ||
                    orderQuantity <= 0
            ) {
                throw new IllegalArgumentException(
                        "주문 수량은 1개 이상이어야 합니다."
                );
            }

            var product =
                    orderItem.getProduct();

            Collection<InventoryItem> inventoryItems =
                    product.getInventoryItems();

            if (
                    inventoryItems == null ||
                    inventoryItems.isEmpty()
            ) {
                throw new IllegalArgumentException(
                        "상품에 연결된 재고 품목이 없습니다."
                );
            }

            /*
             * 상품에 연결된 재고 품목 중
             * 하나라도 재고가 0 이하이면
             * 해당 지점 상품을 품절 처리합니다.
             */
            boolean soldOut = false;

            for (
                    InventoryItem item
                    : inventoryItems
            ) {

                if (item == null) {
                    continue;
                }

                StoreInventory inventory =
                        inventoryMapper
                                .findByStore_IdAndItem_Id(
                                        order.getStore()
                                                .getId(),
                                        item.getId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "해당 지점의 재고 정보가 없습니다."
                                        )
                                );

                /*
                 * 차감하기 전의 부족 상태
                 */
                boolean wasLowStock =
                        inventory.isLowStock();

                /*
                 * 1. 주문 수량만큼 한 번만 차감
                 */
                inventory.decreaseStock(
                        orderQuantity
                );

                /*
                 * 차감 후의 부족 상태
                 */
                boolean isLowStock =
                        inventory.isLowStock();

                /*
                 * 정상 상태에서 부족 상태로
                 * 처음 변경된 시점에만 알림을 생성합니다.
                 *
                 * 이미 부족한 상태에서 추가 주문이 발생해도
                 * 동일한 부족 알림을 반복 생성하지 않습니다.
                 */
                if (
                        !wasLowStock &&
                        isLowStock
                ) {

                    headNotificationService
                            .createLowStockNotification(
                                    inventory.getId(),
                                    getStoreName(
                                            inventory
                                    ),
                                    getProductName(
                                            inventory
                                    ),
                                    inventory.getCurrentStock(),
                                    inventory.getMinStock()
                            );
                }

                /*
                 * 2. THRESHOLD 또는 BOTH 방식이면
                 * 최소 재고 이하인지 검사하여
                 * 목표 재고까지 자동 보충합니다.
                 *
                 * 보충되지 않으면 0을 반환합니다.
                 */
                Integer restockedQuantity =
                        autoRestockService
                                .processThresholdRestock(
                                        inventory
                                );

                /*
                 * 자동 보충 처리 후에도
                 * 실제 재고가 0 이하이면 품절입니다.
                 */
                Integer currentStock =
                        inventory.getCurrentStock();

                if (
                        currentStock == null ||
                        currentStock <= 0
                ) {
                    soldOut = true;
                }

                /*
                 * 현재는 기록 확인용 변수입니다.
                 * 자동 보충 수량은 AutoRestockService에서
                 * 로그와 알림으로 처리합니다.
                 */
                if (
                        restockedQuantity == null
                ) {
                    throw new IllegalStateException(
                            "자동 보충 처리 결과가 없습니다."
                    );
                }
            }

            /*
             * 해당 지점에서 판매하는 상품 조회
             */
            StoreProduct storeProduct =
                    storeProductMapper
                            .findByStoreIdAndProductId(
                                    order.getStore()
                                            .getId(),
                                    product.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "지점 판매 상품 정보가 없습니다."
                                    )
                            );

            /*
             * 최종 품절 상태 반영
             */
            storeProduct.changeSoldOut(
                    soldOut
            );
        }
    }

    /*
     * 주문 기본값 검증
     */
    private void validateOrder(
            Order order
    ) {

        if (order == null) {
            throw new IllegalArgumentException(
                    "주문 정보가 없습니다."
            );
        }

        if (order.getStore() == null) {
            throw new IllegalArgumentException(
                    "주문 지점 정보가 없습니다."
            );
        }

        if (order.getStore().getId() == null) {
            throw new IllegalArgumentException(
                    "주문 지점 번호가 없습니다."
            );
        }
    }

    /*
     * 알림에 표시할 지점명 조회
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
     * 알림에 표시할 상품명 조회
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
                !item.getItemName().isBlank()
        ) {
            return item.getItemName()
                    .trim();
        }

        return "상품명 없음";
    }
}