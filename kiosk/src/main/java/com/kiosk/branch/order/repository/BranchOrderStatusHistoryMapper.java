package com.kiosk.branch.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.OrderStatusHistory;


public interface BranchOrderStatusHistoryMapper 
        extends JpaRepository<OrderStatusHistory, Integer> {

}