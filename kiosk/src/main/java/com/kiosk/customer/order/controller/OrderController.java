package com.kiosk.customer.order.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.order.dto.OrderCreateRequest;
import com.kiosk.customer.order.dto.OrderItemDTO;
import com.kiosk.customer.order.dto.OrderResponse;
import com.kiosk.customer.order.repository.OrderMapper;
import com.kiosk.customer.order.service.OrderService;
import com.kiosk.entity.OrderItem;

import jakarta.servlet.http.HttpSession;
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
    public ResponseEntity<String> completePayment(@PathVariable int orderId, @RequestBody String paymentMethod) {
        // 1. 상세 항목 조회를 서비스 내부로 넘기고, 서비스가 결제 전체를 담당하게 합니다.
        orderService.processPayment(orderId);
        
        return ResponseEntity.ok("결제 및 재고 차감 완료");
    }
  
    @PostMapping
    public ResponseEntity<Integer> createOrder(@RequestBody OrderCreateRequest request, HttpSession session) {
        // 주문을 생성하고 생성된 주문 객체를 반환하도록 서비스 수정
        int orderNumber = orderService.createOrder(request, session);
        
        // 프론트엔드에게 주문번호(Integer)를 전달
        return ResponseEntity.ok(orderNumber);
    }
}

