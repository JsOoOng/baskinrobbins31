package com.kiosk.customer.order.repository;

import com.kiosk.entity.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemOptionMapper extends JpaRepository<OrderItemOption, Integer> {
}