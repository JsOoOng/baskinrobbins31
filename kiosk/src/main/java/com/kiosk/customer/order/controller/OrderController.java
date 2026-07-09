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

    // 2. 결제 완료 처리
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<String> completePayment(@PathVariable int orderId, @RequestBody String paymentMethod) {
        // 1. 주문 ID를 통해 현재 주문된 상품 목록을 다시 DB에서 가져옵니다.
        List<OrderItem> orderItems = orderMapper.selectOrderItemsByOrderId(orderId);
        
        // 2. 서비스에 적절한 파라미터를 넘겨 호출합니다.
        orderService.processPayment(orderId, orderItems);
        
        return ResponseEntity.ok("결제 및 재고 차감 완료");
    }
  
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderCreateRequest request, HttpSession session) {
        
        // 서비스로 결제 정보와 세션(장바구니)을 넘김
        orderService.createOrder(request, session);
        
        return ResponseEntity.ok("주문이 성공적으로 접수되었습니다.");
    }
}

