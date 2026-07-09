package com.kiosk.customer.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kiosk.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

}