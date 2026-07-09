package com.kiosk.customer.order.controller;

import com.kiosk.customer.order.dto.OrderCreateRequest;
import com.kiosk.customer.order.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderCreateRequest request, HttpSession session) {
        
        // 서비스로 결제 정보와 세션(장바구니)을 넘김
        orderService.createOrder(request, session);
        
        return ResponseEntity.ok("주문이 성공적으로 접수되었습니다.");
    }
}