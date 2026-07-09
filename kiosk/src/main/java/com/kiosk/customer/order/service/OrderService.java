package com.kiosk.customer.order.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.customer.order.dto.OrderItem;
import com.kiosk.customer.order.dto.OrderResponse;
import com.kiosk.customer.order.repository.OrderMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;

    /**
     * 1. 주문 내역 및 총금액 조회
     */
    public OrderResponse getOrderDetails(int orderId) {
        // 주문 기본 정보(총금액 포함) 조회
        OrderResponse response = orderMapper.selectOrderById(orderId);
        
        // 주문에 포함된 상품 항목들 조회
        if (response != null) {
            List<OrderItem> items = orderMapper.selectOrderItemsByOrderId(orderId);
            response.setOrderItems(items);
        }
        
        return response;
    }

    /**
     * 2. 결제 완료 및 재고 차감 처리
     */
    @Transactional
    public void processPayment(int orderId, List<OrderItem> orderItems) {
        // A. 주문 항목 순회하며 재고 차감
        for (OrderItem item : orderItems) {
            orderMapper.decreaseProductStock(item.getProductId(), item.getQuantity());
        }
        
        // B. 주문 상태를 'COMPLETED'로 업데이트
        orderMapper.updateOrderStatus(orderId, "COMPLETED");
        
        // C. (필요 시) 결제 테이블 기록 로직 추가 가능
    }
}