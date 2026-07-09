package com.kiosk.customer.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.OrderItem;

@Repository
public interface OrderItemMapper extends JpaRepository<OrderItem, Integer> {
}