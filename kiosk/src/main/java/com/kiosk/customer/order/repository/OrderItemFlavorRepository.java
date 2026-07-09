package com.kiosk.customer.order.repository;

import com.kiosk.entity.OrderItemFlavor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemFlavorRepository extends JpaRepository<OrderItemFlavor, Integer> {
}