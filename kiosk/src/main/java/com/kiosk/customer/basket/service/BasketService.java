package com.kiosk.customer.basket.service;

import com.kiosk.customer.basket.dto.BasketAddRequest;
import com.kiosk.customer.basket.dto.BasketResponse;
import com.kiosk.customer.product.repository.ProductRepository; 
import com.kiosk.customer.product.repository.IcecreamFlavorRepository;
import com.kiosk.entity.Product;
import com.kiosk.entity.IcecreamFlavor;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BasketService {

    private static final String BASKET_SESSION_KEY = "USER_BASKET";
    
    // 🌟 JPA Repository 주입 (DB에서 이름을 꺼내기 위해 필요함)
    private final ProductRepository productRepository;
    private final IcecreamFlavorRepository flavorRepository;

    // 1. 장바구니에 상품 추가 (수정됨)
    public void addItem(HttpSession session, BasketAddRequest request) {
        List<BasketAddRequest> basket = getBasketFromSession(session);
        if (basket == null) basket = new ArrayList<>();
        if (request.getUnitPrice() == null) request.setUnitPrice(0);
        if (request.getQuantity() == null) request.setQuantity(1);
        
        // 🌟 장바구니에 담는 순간 DB를 조회해서 이름을 채워 넣음
        // 1-1. 상품 이름 조회 및 세팅
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId()).orElse(null);
            if (product != null) {
                request.setProductName(product.getProductName()); // 엔티티의 이름 가져오기 메서드 확인 필요
            }
        }

        // 1-2. 맛 이름 조회 및 세팅
        if (request.getFlavors() != null && !request.getFlavors().isEmpty()) {
            for (BasketAddRequest.FlavorDto flavorDto : request.getFlavors()) {
                if (flavorDto.getFlavorId() != null) {
                    IcecreamFlavor flavor = flavorRepository.findById(flavorDto.getFlavorId()).orElse(null);
                    if (flavor != null) {
                        flavorDto.setFlavorName(flavor.getFlavorName()); // 엔티티의 이름 가져오기 메서드 확인 필요
                    }
                }
            }
        }
        
        basket.add(request);
        session.setAttribute(BASKET_SESSION_KEY, basket);
    }

    // 2. 현재 장바구니 조회 (이름 세팅 로직을 addItem으로 옮겼으므로 간단해짐)
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

    // [기존 코드 유지: clearBasket, updateQuantity, removeItem, getBasketFromSession]
    public void clearBasket(HttpSession session) {
        session.removeAttribute(BASKET_SESSION_KEY);
    }
    
    public void updateQuantity(HttpSession session, int index, int quantity) {
        List<BasketAddRequest> basket = getBasketFromSession(session); 
        if (basket != null && index >= 0 && index < basket.size()) {
            basket.get(index).setQuantity(quantity);
            session.setAttribute(BASKET_SESSION_KEY, basket); 
        }
    }

    public void removeItem(HttpSession session, int index) {
        List<BasketAddRequest> basket = getBasketFromSession(session);
        if (basket != null && index >= 0 && index < basket.size()) {
            basket.remove(index); 
            session.setAttribute(BASKET_SESSION_KEY, basket); 
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<BasketAddRequest> getBasketFromSession(HttpSession session) {
        List<BasketAddRequest> basket = (List<BasketAddRequest>) session.getAttribute(BASKET_SESSION_KEY);
        if (basket == null) {
            basket = new ArrayList<>();
        }
        return basket;
    }
}