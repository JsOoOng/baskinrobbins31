package com.kiosk.customer.basket.service;

import com.kiosk.customer.basket.dto.BasketAddRequest;
import com.kiosk.customer.basket.dto.BasketResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BasketService {

    private static final String BASKET_SESSION_KEY = "USER_BASKET";

    // 1. 장바구니에 상품 추가
    public void addItem(HttpSession session, BasketAddRequest request) {
        // 1. 세션에서 리스트를 꺼내되, 만약 없으면 새 리스트(ArrayList)를 생성해서 가져옴
        List<BasketAddRequest> basket = getBasketFromSession(session);
        if (basket == null) {
            basket = new ArrayList<>();
        }

        // 2. 단가(unitPrice)가 null인지 체크해서 0으로 보정 (에러 방지)
        if (request.getUnitPrice() == null) {
            request.setUnitPrice(0); 
        }
        if (request.getQuantity() == null) {
            request.setQuantity(1);
        }

        // 3. 상품 추가
        basket.add(request);

        // 4. 세션에 다시 저장
        session.setAttribute(BASKET_SESSION_KEY, basket);
    }

    // 2. 현재 장바구니 조회
    public BasketResponse getBasket(HttpSession session) {
        List<BasketAddRequest> basket = getBasketFromSession(session);
        
        int totalPrice = basket.stream()
                .mapToInt(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
                
        return BasketResponse.builder()
                .items(basket)
                .totalPrice(totalPrice)
                .totalQuantity(basket.size())
                .build();
    }

    // 3. 장바구니 특정 상품 삭제
    public void removeItem(HttpSession session, int index) {
        List<BasketAddRequest> basket = getBasketFromSession(session);
        if (index >= 0 && index < basket.size()) {
            basket.remove(index);
            session.setAttribute(BASKET_SESSION_KEY, basket);
        }
    }

    // 4. 장바구니 특정 상품 수량 변경
    public void updateItemQuantity(HttpSession session, int index, int quantity) {
        List<BasketAddRequest> basket = getBasketFromSession(session);
        if (index >= 0 && index < basket.size()) {
            if (quantity >= 1) {
                basket.get(index).setQuantity(quantity);
                session.setAttribute(BASKET_SESSION_KEY, basket);
            }
        }
    }

    // 5. 장바구니 초기화 (결제 완료 후 또는 전체 삭제 시)
    public void clearBasket(HttpSession session) {
        session.removeAttribute(BASKET_SESSION_KEY);
    }

    // 세션에서 장바구니 리스트 가져오기 (없으면 새로 생성)
    @SuppressWarnings("unchecked")
    private List<BasketAddRequest> getBasketFromSession(HttpSession session) {
        List<BasketAddRequest> basket = (List<BasketAddRequest>) session.getAttribute(BASKET_SESSION_KEY);
        if (basket == null) {
            basket = new ArrayList<>();
        }
        return basket;
    }
    

    public void updateQuantity(HttpSession session, int index, int quantity) {
        BasketResponse basket = getBasket(session); // 세션에서 장바구니 꺼내기
        if (basket != null && index >= 0 && index < basket.getItems().size()) {
            BasketAddRequest item = basket.getItems().get(index);
            item.setQuantity(quantity); // 수량 업데이트
            recalculateTotalPrice(basket);
        }
    }

    public void removeItem(HttpSession session, int index) {
        BasketResponse basket = getBasket(session);
        if (basket != null && index >= 0 && index < basket.getItems().size()) {
            basket.getItems().remove(index); // 리스트에서 제거
            recalculateTotalPrice(basket);
        }
    }
    
    private void recalculateTotalPrice(BasketResponse basket) {
        int total = basket.getItems().stream()
                .mapToInt(item -> {
                    // 💡 누가 범인인지 범인 색출!
                    if (item.getUnitPrice() == null) {
                        System.out.println("🚨 범인 발견: productId가 " + item.getProductId() + "인 상품의 unitPrice가 null입니다!");
                        return 0;
                    }
                    int qty = (item.getQuantity() != null) ? item.getQuantity() : 0;
                    return item.getUnitPrice() * qty;
                })
                .sum();
        basket.setTotalPrice(total);
    }
}