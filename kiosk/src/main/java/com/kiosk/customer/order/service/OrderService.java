package com.kiosk.customer.order.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.customer.basket.dto.BasketAddRequest;
import com.kiosk.customer.basket.dto.BasketResponse;
import com.kiosk.customer.basket.service.BasketService;
import com.kiosk.customer.order.dto.OrderCreateRequest;
import com.kiosk.customer.order.dto.OrderItemDTO;
import com.kiosk.customer.order.dto.OrderResponse;
import com.kiosk.customer.order.repository.OrderItemFlavorMapper;
import com.kiosk.customer.order.repository.OrderItemMapper;
import com.kiosk.customer.order.repository.OrderItemOptionMapper;
import com.kiosk.customer.order.repository.OrderMapper;
import com.kiosk.customer.order.repository.OrderRepository;
import com.kiosk.entity.IcecreamFlavor;
import com.kiosk.entity.Kiosk;
import com.kiosk.entity.Order;
import com.kiosk.entity.OrderItem;
import com.kiosk.entity.OrderItemFlavor;
import com.kiosk.entity.OrderItemOption;
import com.kiosk.entity.Product;
import com.kiosk.entity.ProductOption;
import com.kiosk.entity.Store;
import com.kiosk.entity.User;
import com.kiosk.entity.enums.OrderStatus;
import com.kiosk.entity.enums.OrderType;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
  
    private final BasketService basketService;
    private final EntityManager em; // 숫자 ID를 참조 객체로 변환하기 위해 사용


    // 주문 관련 4개의 Repository
    private final OrderItemMapper orderItemRepository;
    private final OrderItemFlavorMapper orderItemFlavorRepository;
    private final OrderItemOptionMapper orderItemOptionRepository;
    private final OrderRepository orderRepository;
    
    /**
     * 1. 주문 내역 및 총금액 조회
     */
 // 변경 후 (깔끔하고 효율적)
    public OrderResponse getOrderDetails(int orderId) {
        // 쿼리 하나로 모든 데이터(Order, Items, Flavors)가 담긴 객체를 가져옵니다.
        return orderMapper.selectOrderWithDetails(orderId);
    }

    /**
     * 2. 주문 생성 로직
     */
    @Transactional
    public int createOrder(OrderCreateRequest request, HttpSession session) {
    	
        // 1. 세션에서 장바구니 데이터 꺼내기 및 예외 처리
        BasketResponse basket = basketService.getBasket(session);
        if (basket == null || basket.getItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다. 주문할 상품을 추가해주세요.");
        }

        // 2. 주문 번호 생성 (저장 직전에 생성하여 번호 꼬임 방지)
        int nextOrderNumber = generateDailyOrderNumber();

        // 3. DTO의 ID들을 JPA 엔티티 참조 객체(Proxy)로 변환
        Store store = em.find(Store.class, request.getStoreId());
        if (store == null) {
            throw new IllegalArgumentException("유효하지 않은 매장 정보입니다.");
        }

        Kiosk kiosk = request.getKioskId() != null ? em.find(Kiosk.class, request.getKioskId()) : null;
        User user = request.getUserId() != null ? em.find(User.class, request.getUserId()) : null;

        // 4. 주문(Order) 마스터 생성 및 저장
        Order order = Order.builder()
                .store(store)
                .kiosk(kiosk)
                .user(user)
                .orderNumber(nextOrderNumber) // 생성된 번호 저장
                .orderType(OrderType.valueOf(request.getOrderType()))
                .dryIceMins(request.getDryIceMins())
                .orderStatus(OrderStatus.WAITING)
                .totalPrice(basket.getTotalPrice())
                .build();
        
        orderRepository.save(order); 

        // 5. 장바구니 리스트를 돌면서 OrderItem 저장
        for (BasketAddRequest basketItem : basket.getItems()) {
            Product product = em.find(Product.class, basketItem.getProductId());
            if (product == null) {
                throw new RuntimeException("상품 정보를 찾을 수 없습니다: " + basketItem.getProductId());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(basketItem.getQuantity())
                    .unitPrice(basketItem.getUnitPrice())
                    .build();

            orderItemRepository.save(orderItem);

            // 선택한 맛(Flavor)이 있다면 저장
            if (basketItem.getFlavors() != null) {
                for (BasketAddRequest.FlavorDto flavorDto : basketItem.getFlavors()) {
                    IcecreamFlavor flavor = em.find(IcecreamFlavor.class, flavorDto.getFlavorId());
                    if (flavor != null) {
                        OrderItemFlavor itemFlavor = OrderItemFlavor.builder()
                                .orderItem(orderItem)
                                .flavor(flavor)
                                .quantity(flavorDto.getQuantity())
                                .build();
                        orderItemFlavorRepository.save(itemFlavor);
                    }
                }
            }

            // 선택한 옵션(Option)이 있다면 저장
            if (basketItem.getOptions() != null) {
                for (Integer optionId : basketItem.getOptions()) {
                    ProductOption option = em.find(ProductOption.class, optionId);
                    if (option != null) {
                        OrderItemOption itemOption = OrderItemOption.builder()
                                .orderItem(orderItem)
                                .option(option)
                                .build();
                        orderItemOptionRepository.save(itemOption);
                    }
                }
            }
        }

        // 6. 주문 성공 후 세션 비우기
        basketService.clearBasket(session);

        //생성된 주문 번호 반환
        return nextOrderNumber;
    }
    
    // 주문번호 1씩 증가
    private int generateDailyOrderNumber() {
        Integer lastNumber = orderMapper.selectLastOrderNumberForToday();
        
        // 만약 데이터가 하나도 없으면 1, 있으면 마지막 번호에 1을 더함
        return lastNumber + 1;
    }
    
    @Transactional
    public void processPayment(int orderId) {
        // 내부에서 조회하므로 인자로 리스트를 받을 필요가 없습니다.
        OrderResponse order = orderMapper.selectOrderWithDetails(orderId);
        
        // 이후 재고 차감 및 상태 변경 로직...
        for (OrderItemDTO item : order.getOrderItems()) {
            int updatedRows = orderMapper.decreaseProductStock(item.getProductId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new RuntimeException("상품 [" + item.getProductName() + "]의 재고가 부족합니다.");
            }
        }
        orderMapper.updateOrderStatus(orderId, "COMPLETED");
    }
    

    /**
     * 3. 결제 처리 및 재고 차감 (예외 처리 통합 버전)
     */
    @Transactional
    public void processPayment(int orderId, String paymentMethod) {
        // 1. 주문 조회 및 존재 여부 검증
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("해당 주문번호가 존재하지 않습니다: " + orderId));

        // 2. 주문 상태 검증 (중복 결제 방지)
        if (OrderStatus.COMPLETED.equals(order.getOrderStatus())) {
            throw new IllegalStateException("이미 결제 처리된 주문입니다.");
        }

        // 3. 결제 수단 확인
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            throw new IllegalArgumentException("결제 수단을 선택해주세요.");
        }

        // 4. 외부 연동 가상 시나리오 (네트워크/시간 초과 등)
        // 실제 결제 API 연동 시 try-catch 블록으로 감싸서 예외 처리
        try {
            // 외부 결제 모듈 호출 시뮬레이션
            // requestPayment(paymentMethod); 
        } catch (Exception e) {
            // 통신 장애 시
            throw new RuntimeException("네트워크 연결이 불안정합니다.");
        }

     // 5. 주문 상세 정보와 상품 목록을 한 번에 조회
        OrderResponse orderRes = orderMapper.selectOrderWithDetails(orderId);

        // 통합 객체에서 상품 리스트만 추출
        List<OrderItemDTO> orderItems = orderRes.getOrderItems();

        // 상품 리스트로 재고 차감 로직 수행
        for (OrderItemDTO item : orderItems) {
            int updatedRows = orderMapper.decreaseProductStock(item.getProductId(), item.getQuantity());
            
            // 재고가 없으면 예외 발생 (결제 전체 롤백)
            if (updatedRows == 0) {
                throw new RuntimeException("상품 [" + item.getProductName() + "]의 재고가 부족합니다.");
            }
        }

        // 6. 주문 상태 완료 업데이트
        orderMapper.updateOrderStatus(orderId, "COMPLETED");
        
        System.out.println("결제 성공: 주문번호=" + orderId);
    }
    
   
}