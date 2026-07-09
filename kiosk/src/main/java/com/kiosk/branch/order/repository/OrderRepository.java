package com.kiosk.branch.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Order;


public interface OrderRepository extends JpaRepository<Order, Integer> {


    @EntityGraph(attributePaths = {
        "orderItems",
        "orderItems.product"
    })
    Optional<Order> findWithItemsByOrderId(Integer orderId);
    
 // 특정 지점 주문 조회
    List<Order> findByStoreStoreIdOrderByCreatedAtDesc(Integer storeId);
    
}