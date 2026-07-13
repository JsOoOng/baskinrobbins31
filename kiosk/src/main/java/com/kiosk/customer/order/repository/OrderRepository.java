package com.kiosk.customer.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kiosk.entity.Order;

import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    @Query("SELECT COALESCE(MAX(o.orderNumber), 0) FROM Order o")
    Integer findMaxOrderNumber();

}