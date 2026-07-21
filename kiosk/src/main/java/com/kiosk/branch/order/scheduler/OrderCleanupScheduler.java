package com.kiosk.branch.order.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kiosk.branch.order.repository.BranchOrderMapper;
import com.kiosk.entity.Order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderCleanupScheduler {

    private final BranchOrderMapper orderMapper;

    @Transactional
    @Scheduled(cron = "0 0 10 * * *")
    public void deleteNullPaymentOrders() {

        List<Order> orders = orderMapper.findByPaymentIsNull();

        System.out.println("삭제 대상 주문 수 : " + orders.size());

        for (Order order : orders) {
            System.out.println("삭제 시도 : " + order.getId());
            orderMapper.delete(order);
        }

        orderMapper.flush();

        System.out.println("삭제 완료");
    }
}