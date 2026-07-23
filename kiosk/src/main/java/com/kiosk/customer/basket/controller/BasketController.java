package com.kiosk.customer.basket.controller;

import com.kiosk.customer.basket.dto.BasketAddRequest;
import com.kiosk.customer.basket.dto.BasketResponse;
import com.kiosk.customer.basket.service.BasketService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * [코드 흐름 안내] BasketController
 *
 * <p>역할: 고객 키오스크의 장바구니 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/customer/basket) -> BasketService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/customer/basket")
@RequiredArgsConstructor
public class BasketController {

    private final BasketService basketService;

    // 장바구니 조회 API
    /**
     * [요청 흐름] GET /api/customer/basket
     * 프론트 요청을 받아 getBasket() 메서드가 입력을 받고 BasketService 호출 후 결과를 응답한다.
     */
    @GetMapping
    public ResponseEntity<BasketResponse> getBasket(HttpSession session) {
        return ResponseEntity.ok(basketService.getBasket(session));
    }

 // 장바구니 상품 추가 API
    /**
     * [요청 흐름] POST /api/customer/basket
     * 프론트 요청을 받아 addItem() 메서드가 입력을 받고 BasketService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public ResponseEntity<String> addItem(@RequestBody BasketAddRequest request, HttpSession session) {
        
        // 💡 이 코드를 한 줄 추가해 보세요!
        System.out.println("프론트엔드 상품 ID : " + request.getProductId());
        
        basketService.addItem(session, request);
        return ResponseEntity.ok("장바구니에 담겼습니다.\n"); // \n을 붙이면 CMD에서 잘 보입니다.
    }

    // 장바구니 특정 상품 삭제 API
    /**
     * [요청 흐름] DELETE /api/customer/basket/{index}
     * 프론트 요청을 받아 removeItem() 메서드가 입력을 받고 BasketService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/{index}")
    public ResponseEntity<String> removeItem(@PathVariable int index, HttpSession session) {
        basketService.removeItem(session, index);
        return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");
    }

    // 장바구니 특정 상품 수량 변경 API
    /**
     * [요청 흐름] PUT /api/customer/basket/{index}
     * 프론트 요청을 받아 updateItemQuantity() 메서드가 입력을 받고 BasketService 호출 후 결과를 응답한다.
     */
    @PutMapping("/{index}")
    public ResponseEntity<String> updateItemQuantity(@PathVariable int index, @RequestParam int quantity, HttpSession session) {
        basketService.updateItemQuantity(session, index, quantity);
        return ResponseEntity.ok("상품 수량이 변경되었습니다.");
    }

    // 장바구니 특정 상품 옵션/맛 전체 변경 API
    /**
     * [요청 흐름] PUT /api/customer/basket/item/{index}
     * 프론트 요청을 받아 updateItemOptions() 메서드가 입력을 받고 BasketService 호출 후 결과를 응답한다.
     */
    @PutMapping("/item/{index}")
    public ResponseEntity<String> updateItemOptions(@PathVariable int index, @RequestBody BasketAddRequest request, HttpSession session) {
        basketService.updateItemOptions(session, index, request);
        return ResponseEntity.ok("상품 옵션이 변경되었습니다.");
    }

    // 장바구니 전체 비우기 API
    /**
     * [요청 흐름] DELETE /api/customer/basket
     * 프론트 요청을 받아 clearBasket() 메서드가 입력을 받고 BasketService 호출 후 결과를 응답한다.
     */
    @DeleteMapping
    public ResponseEntity<String> clearBasket(HttpSession session) {
        basketService.clearBasket(session);
        return ResponseEntity.ok("장바구니가 비워졌습니다.");
    }


}