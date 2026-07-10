package com.kiosk.customer.order.repository;

import com.kiosk.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, Integer> {

	// 특정 매장(storeId)의 오늘 하루(startOfDay ~ endOfDay) 주문 건수 카운트
    @Query("SELECT COUNT(o) FROM Order o WHERE o.store.id = :storeId AND o.createdAt >= :startOfDay AND o.createdAt <= :endOfDay")
    int countTodayOrders(
        @Param("storeId") Integer storeId, 
        @Param("startOfDay") LocalDateTime startOfDay, 
        @Param("endOfDay") LocalDateTime endOfDay
    );
}