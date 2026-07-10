package com.kiosk.customer.order.service;

import java.time.LocalDateTime;
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
    public OrderResponse getOrderDetails(int orderId) {
        // 1. 주문 기본 정보 조회
        OrderResponse response = orderMapper.selectOrderById(orderId);
        
        // 2. 주문에 포함된 상품 항목들 조회 (이미 DTO로 반환되므로 바로 받습니다)
        if (response != null) {
            // List<OrderItem> 이 아니라 List<OrderItemDTO> 로 받아야 합니다.
            List<OrderItemDTO> itemDtos = orderMapper.selectOrderItemsByOrderId(orderId);
            response.setOrderItems(itemDtos);
        }
        
        return response;
    }
 // OrderService.java 내의 수정할 메서드
    @Transactional
    public void processPayment(int orderId, List<OrderItemDTO> orderItems) {
        System.out.println("결제 처리 시작: 주문번호=" + orderId + ", 항목수=" + orderItems.size());
        for (OrderItemDTO item : orderItems) {
            System.out.println("재고 차감 시도: 상품ID=" + item.getProductId() + ", 수량=" + item.getQuantity());
            orderMapper.decreaseProductStock(item.getProductId(), item.getQuantity());
        }
        orderMapper.updateOrderStatus(orderId, "COMPLETED");
    }
    

    @Transactional
    public void createOrder(OrderCreateRequest request, HttpSession session) {

        // 1. 세션에서 장바구니 데이터 꺼내기
        BasketResponse basket = basketService.getBasket(session);
        if (basket.getItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다.");
        }

        // 2. DTO의 숫자 ID들을 JPA 엔티티 참조 객체(Proxy)로 변환
        Store store = em.getReference(Store.class, request.getStoreId());
        Kiosk kiosk = request.getKioskId() != null ? em.getReference(Kiosk.class, request.getKioskId()) : null;
        User user = request.getUserId() != null ? em.getReference(User.class, request.getUserId()) : null;

        // 🌟 [추가된 부분] 당일 주문번호(영수증 번호) 생성 로직
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        
        // DB에서 오늘 해당 지점의 주문 개수를 가져와서 + 1
        int todayOrderCount = orderRepository.countTodayOrders(request.getStoreId(), startOfDay, endOfDay);
        int nextOrderNumber = todayOrderCount + 1;
        
        // 3. 주문(Order) 마스터 생성 및 저장
        Order order = Order.builder()
                .store(store)
                .kiosk(kiosk)
                .user(user)
                .orderNumber(nextOrderNumber) // TODO: 당일 영수증 번호 생성 로직 필요
                .orderType(OrderType.valueOf(request.getOrderType()))
                .dryIceMins(request.getDryIceMins())
                .orderStatus(OrderStatus.WAITING)
                .totalPrice(basket.getTotalPrice())
                .build();
        
        // ⭐️ 여기서 먼저 Save를 해야 Order에 id(PK)가 발급됩니다.
        orderRepository.save(order); 

        // 4. 장바구니 리스트를 돌면서 OrderItem 저장
        for (BasketAddRequest basketItem : basket.getItems()) {
            Product product = em.getReference(Product.class, basketItem.getProductId());

            OrderItem orderItem = OrderItem.builder()
                    .order(order) // 방금 저장해서 PK가 생긴 order 연결
                    .product(product)
                    .quantity(basketItem.getQuantity())
                    .unitPrice(basketItem.getUnitPrice())
                    .build();

            orderItemRepository.save(orderItem);

            // 5. 선택한 맛(Flavor)이 있다면 저장
            if (basketItem.getFlavors() != null) {
                for (BasketAddRequest.FlavorDto flavorDto : basketItem.getFlavors()) {
                    IcecreamFlavor flavor = em.getReference(IcecreamFlavor.class, flavorDto.getFlavorId());
                    
                    OrderItemFlavor itemFlavor = OrderItemFlavor.builder()
                            .orderItem(orderItem)
                            .flavor(flavor)
                            .quantity(flavorDto.getQuantity())
                            .build();
                    orderItemFlavorRepository.save(itemFlavor);
                }
            }

            // 6. 선택한 옵션(Option)이 있다면 저장
            if (basketItem.getOptions() != null) {
                for (Integer optionId : basketItem.getOptions()) {
                    ProductOption option = em.getReference(ProductOption.class, optionId);
                    
                    OrderItemOption itemOption = OrderItemOption.builder()
                            .orderItem(orderItem)
                            .option(option)
                            .build();
                    orderItemOptionRepository.save(itemOption);
                }
            }
        }

        // 7. 주문이 성공했으니 세션 장바구니 비우기
        basketService.clearBasket(session);
    }
}