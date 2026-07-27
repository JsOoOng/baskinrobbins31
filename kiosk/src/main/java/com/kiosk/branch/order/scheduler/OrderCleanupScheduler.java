package com.kiosk.branch.order.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kiosk.branch.order.repository.BranchOrderMapper;
import com.kiosk.entity.Order;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] OrderCleanupScheduler
 *
 * <p>역할: 지점 운영의 주문 처리를 보조하는 class다.</p>
 * <p>호출 흐름: 호출하는 클래스에서 필요한 값을 받아 현재 메서드의 결과를 다음 계층으로 전달한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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