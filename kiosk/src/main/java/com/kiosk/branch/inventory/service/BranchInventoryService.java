package com.kiosk.branch.inventory.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.inventory.repository.BranchInventoryMapper;
import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Order;
import com.kiosk.entity.OrderItem;
import com.kiosk.entity.StoreInventory;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class BranchInventoryService {


    private final BranchInventoryMapper inventoryMapper;



    public void decrease(Order order){


        for(OrderItem orderItem 
                : order.getOrderItems()){


            // 주문한 상품
            var product =
                    orderItem.getProduct();



            // 해당 상품의 재고 품목
            for(InventoryItem item 
                    : product.getInventoryItems()){


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


                inventory.decreaseStock(
                        orderItem.getQuantity()
                );


            }

        }

    }

}