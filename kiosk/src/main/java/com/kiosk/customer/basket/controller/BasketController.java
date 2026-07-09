package com.kiosk.customer.basket.controller;

import com.kiosk.customer.basket.dto.BasketAddRequest;
import com.kiosk.customer.basket.dto.BasketResponse;
import com.kiosk.customer.basket.service.BasketService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/basket")
@RequiredArgsConstructor
public class BasketController {

    private final BasketService basketService;

    // 장바구니 조회 API
    @GetMapping
    public ResponseEntity<BasketResponse> getBasket(HttpSession session) {
        return ResponseEntity.ok(basketService.getBasket(session));
    }

 // 장바구니 상품 추가 API
    @PostMapping
    public ResponseEntity<String> addItem(@RequestBody BasketAddRequest request, HttpSession session) {
        
        // 💡 이 코드를 한 줄 추가해 보세요!
        System.out.println("프론트엔드 상품 ID : " + request.getProductId());
        
        basketService.addItem(session, request);
        return ResponseEntity.ok("장바구니에 담겼습니다.\n"); // \n을 붙이면 CMD에서 잘 보입니다.
    }

    // 장바구니 전체 비우기 API
    @DeleteMapping
    public ResponseEntity<String> clearBasket(HttpSession session) {
        basketService.clearBasket(session);
        return ResponseEntity.ok("장바구니가 비워졌습니다.");
    }
}