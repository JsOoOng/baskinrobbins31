package com.kiosk.branch.inventory.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.inventory.repository.BranchInventoryMapper;
import com.kiosk.branch.status.reopsitory.StoreProductMapper;
import com.kiosk.common.inventory.AutoRestockService;
import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Order;
import com.kiosk.entity.OrderItem;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.StoreProduct;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class BranchInventoryService {


    private final BranchInventoryMapper inventoryMapper;
    private final AutoRestockService autoRestockService;

    private final StoreProductMapper storeProductMapper;

    public void decrease(Order order){

        for(OrderItem orderItem : order.getOrderItems()){


            var product = orderItem.getProduct();


            for(InventoryItem item : product.getInventoryItems()){


                StoreInventory inventory =
                        inventoryMapper
                        .findByStore_IdAndItem_Id(
                                order.getStore().getId(),
                                item.getId()
                        )
                        .orElseThrow(
                            () -> new RuntimeException(
                                "재고 없음"
                            )
                        );


                // 재고 차감
                inventory.decreaseStock(
                        orderItem.getQuantity()
                );

                autoRestockService
                        .processThresholdRestock(
                                inventory
                        );

                if (inventory.needsThresholdRestock()) {

                    Integer restockedQuantity =
                            inventory.autoRestock();

                    System.out.println(
                            "임계 재고 자동 보충: "
                                    + "storeInventoryId="
                                    + inventory.getId()
                                    + ", quantity="
                                    + restockedQuantity
                                    + ", currentStock="
                                    + inventory.getCurrentStock()
                    );
                }


                // 재고 0이면 품절 처리
                if(inventory.getCurrentStock() <= 0){


                    StoreProduct storeProduct =
                            storeProductMapper
                            .findByStoreIdAndProductId(
                                    order.getStore().getId(),
                                    product.getId()
                            )
                            .orElseThrow(
                                () -> new RuntimeException(
                                    "지점 상품 없음"
                                )
                            );


                    storeProduct.changeSoldOut(true);

                }
                
                

            }

        }

    }

}