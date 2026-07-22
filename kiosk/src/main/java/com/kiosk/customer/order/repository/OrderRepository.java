package com.kiosk.customer.order.repository;

import com.kiosk.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT COALESCE(MAX(o.orderNumber), 0) FROM Order o WHERE o.store.id = :storeId AND o.createdAt >= :startOfDay")
    Integer findMaxOrderNumberByStoreAndDate(@Param("storeId") Integer storeId, @Param("startOfDay") LocalDateTime startOfDay);

}