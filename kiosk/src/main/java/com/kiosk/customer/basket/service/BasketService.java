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
        List<BasketAddRequest> basket = getBasketFromSession(session);
        // TODO: 이미 있는 상품인지 체크하고 수량만 늘리는 로직 추가 가능
        basket.add(request);
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
}