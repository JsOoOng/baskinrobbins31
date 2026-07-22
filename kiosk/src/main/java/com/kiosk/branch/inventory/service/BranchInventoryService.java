package com.kiosk.branch.inventory.service;

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

    /*
     * 주문 완료 후 재고 차감
     */
    public void decrease(
            Order order
    ) {

        if (
                order == null ||
                order.getStore() == null
        ) {
            throw new IllegalArgumentException(
                    "주문 또는 지점 정보가 없습니다."
            );
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

            /*
             * 상품에 연결된 재고 품목 중
             * 하나라도 재고가 없으면 상품 품절 처리
             */
            boolean soldOut = false;

            for (
                    InventoryItem item
                    : product.getInventoryItems()
            ) {

                StoreInventory inventory =
                        inventoryMapper
                                .findByStore_IdAndItem_Id(
                                        order
                                                .getStore()
                                                .getId(),
                                        item.getId()
                                )
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "해당 지점의 재고 정보가 없습니다."
                                        )
                                );

                /*
                 * 1. 주문 수량만큼 한 번만 차감
                 */
                inventory.decreaseStock(
                        orderQuantity
                );

                /*
                 * 2. THRESHOLD 또는 BOTH이면
                 * 최소 재고 이하인지 검사하여
                 * 목표 재고까지 자동 보충
                 *
                 * DAILY는 이 검사에서 통과하지 않음
                 */
                Integer restockedQuantity =
                        autoRestockService
                                .processThresholdRestock(
                                        inventory
                                );

                /*
                 * 자동 보충되지 않았고
                 * 실제 재고가 0 이하인 경우 품절
                 */
                if (
                        restockedQuantity <= 0 &&
                        inventory.getCurrentStock() <= 0
                ) {
                    soldOut = true;
                }
            }

            /*
             * 연결된 모든 재고를 검사한 결과에 따라
             * 지점 판매 상품 품절 상태 변경
             */
            StoreProduct storeProduct =
                    storeProductMapper
                            .findByStoreIdAndProductId(
                                    order
                                            .getStore()
                                            .getId(),
                                    product.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "지점 판매 상품 정보가 없습니다."
                                    )
                            );

            storeProduct.changeSoldOut(
                    soldOut
            );
        }
    }
}