package com.kiosk.customer.basket.service;

import com.kiosk.customer.basket.dto.BasketAddRequest;
import com.kiosk.customer.basket.dto.BasketResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * [코드 흐름 안내] BasketService
 *
 * <p>역할: 고객 키오스크의 장바구니 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
public class BasketService {

    private static final String BASKET_SESSION_KEY = "USER_BASKET";

    // 1. 장바구니에 상품 추가
    /**
     * [메서드 흐름] addItem
     * Controller 또는 상위 서비스에서 호출되어 입력값을 현재 클래스의 규칙에 따라 처리하고 호출한 곳으로 결과를 반환한다.
     */
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

        // 3. 기존 장바구니에 완벽히 동일한 상품(메뉴, 옵션, 맛, 스푼 등)이 있는지 확인
        boolean found = false;
        for (BasketAddRequest item : basket) {
            if (isSameItem(item, request)) {
                item.setQuantity(item.getQuantity() + request.getQuantity());
                found = true;
                break;
            }
        }

        // 4. 없으면 새로 추가
        if (!found) {
            basket.add(request);
        }

        // 5. 세션에 다시 저장
        session.setAttribute(BASKET_SESSION_KEY, basket);
    }

    // 동일한 메뉴 및 옵션인지 판별하는 헬퍼 메서드
    private boolean isSameItem(BasketAddRequest a, BasketAddRequest b) {
        if (!java.util.Objects.equals(a.getProductId(), b.getProductId())) return false;
        if (!java.util.Objects.equals(a.getExtraSpoons(), b.getExtraSpoons())) return false;
        
        // options 비교 (null 안전 처리)
        List<Integer> optsA = a.getOptions() == null ? java.util.Collections.emptyList() : a.getOptions();
        List<Integer> optsB = b.getOptions() == null ? java.util.Collections.emptyList() : b.getOptions();
        if (optsA.size() != optsB.size() || !optsA.containsAll(optsB)) return false;

        // flavors 비교 (null 안전 처리)
        List<BasketAddRequest.FlavorDto> fA = a.getFlavors() == null ? java.util.Collections.emptyList() : a.getFlavors();
        List<BasketAddRequest.FlavorDto> fB = b.getFlavors() == null ? java.util.Collections.emptyList() : b.getFlavors();
        if (fA.size() != fB.size()) return false;
        
        for (BasketAddRequest.FlavorDto faItem : fA) {
            boolean match = fB.stream().anyMatch(fbItem -> 
                java.util.Objects.equals(faItem.getFlavorId(), fbItem.getFlavorId()) &&
                java.util.Objects.equals(faItem.getQuantity(), fbItem.getQuantity())
            );
            if (!match) return false;
        }
        
        return true;
    }

    // 2. 현재 장바구니 조회
    /**
     * [메서드 흐름] getBasket
     * Controller 또는 상위 서비스에서 호출되어 입력값을 현재 클래스의 규칙에 따라 처리하고 호출한 곳으로 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] removeItem
     * Controller 또는 상위 서비스에서 호출되어 입력값을 현재 클래스의 규칙에 따라 처리하고 호출한 곳으로 결과를 반환한다.
     */
    public void removeItem(HttpSession session, int index) {
        List<BasketAddRequest> basket = getBasketFromSession(session);
        if (index >= 0 && index < basket.size()) {
            basket.remove(index);
            session.setAttribute(BASKET_SESSION_KEY, basket);
        }
    }

    // 4. 장바구니 특정 상품 수량 변경
    /**
     * [메서드 흐름] updateItemQuantity
     * Controller 또는 상위 서비스에서 호출되어 입력값을 현재 클래스의 규칙에 따라 처리하고 호출한 곳으로 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] clearBasket
     * Controller 또는 상위 서비스에서 호출되어 입력값을 현재 클래스의 규칙에 따라 처리하고 호출한 곳으로 결과를 반환한다.
     */
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

    // 6. 장바구니 특정 상품 옵션/맛 완전 변경
    /**
     * [메서드 흐름] updateItemOptions
     * Controller 또는 상위 서비스에서 호출되어 입력값을 현재 클래스의 규칙에 따라 처리하고 호출한 곳으로 결과를 반환한다.
     */
    public void updateItemOptions(HttpSession session, int index, BasketAddRequest request) {
        List<BasketAddRequest> basket = getBasketFromSession(session);
        if (index >= 0 && index < basket.size()) {
            if (request.getUnitPrice() == null) request.setUnitPrice(0); 
            if (request.getQuantity() == null) request.setQuantity(1);

            boolean merged = false;
            for (int i = 0; i < basket.size(); i++) {
                if (i == index) continue; // 자기 자신 제외

                BasketAddRequest existingItem = basket.get(i);
                if (isSameItem(existingItem, request)) {
                    // 동일 구성 발견 시 수량 병합 후 원래 인덱스 삭제
                    existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
                    basket.remove(index);
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                // 병합 대상이 없으면 현재 인덱스의 항목을 새로운 구성으로 교체
                basket.set(index, request);
            }

            session.setAttribute(BASKET_SESSION_KEY, basket);
        }
    }

}