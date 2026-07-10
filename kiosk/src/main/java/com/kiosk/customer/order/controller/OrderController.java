package com.kiosk.customer.order.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.order.dto.OrderResponse;
import com.kiosk.customer.order.repository.OrderMapper;
import com.kiosk.customer.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {
	
	private final OrderService orderService;
	private final OrderMapper orderMapper;
    
	// 1. 주문 내역 및 총금액 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable int orderId) {
        OrderResponse response = orderService.getOrderDetails(orderId);
        
        if (response == null) {
            return ResponseEntity.notFound().build(); // 주문이 없을 경우 404 처리
        }
        
        return ResponseEntity.ok(response); // 주문이 있으면 데이터와 함께 200 반환
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<String> completePayment(
            @PathVariable int orderId, 
            @RequestBody Map<String, String> request) { // Map으로 변경
        
        String paymentMethod = request.get("paymentMethod"); // 키 값으로 추출
        orderService.processPayment(orderId, paymentMethod);
        
        return ResponseEntity.ok("결제 및 재고 차감 완료");
    }
    
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable int orderId) {
        // 주문 상태를 CANCELED로 변경하는 서비스 호출
        orderService.cancelOrder(orderId);
        
        return ResponseEntity.ok("주문 취소 완료");
    }
  
}

