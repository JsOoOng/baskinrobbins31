package com.kiosk.customer.order.service;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.kiosk.customer.basket.dto.BasketAddRequest;
import com.kiosk.customer.basket.dto.BasketResponse;
import com.kiosk.customer.basket.service.BasketService;
import com.kiosk.customer.order.dto.OrderCreateRequest;
import com.kiosk.customer.order.dto.OrderItemDTO;
import com.kiosk.customer.order.dto.OrderResponse;
import com.kiosk.customer.order.dto.Payment;
import com.kiosk.customer.order.repository.OrderItemFlavorMapper;
import com.kiosk.customer.order.repository.OrderItemMapper;
import com.kiosk.customer.order.repository.OrderItemOptionMapper;
import com.kiosk.customer.order.repository.OrderMapper;
import com.kiosk.customer.order.repository.OrderRepository;
import com.kiosk.customer.order.repository.UserRepository;
import java.util.Optional;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final BasketService basketService;
    private final EntityManager em;

    private final OrderItemMapper orderItemRepository;
    private final OrderItemFlavorMapper orderItemFlavorRepository;
    private final OrderItemOptionMapper orderItemOptionRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // 주문 상세 조회
    public OrderResponse getOrderDetails(int orderId) {
        return orderMapper.selectOrderWithDetails(orderId);
    }

    /**
     * 주문 생성 로직
     */
    @Transactional
    public int createOrder(OrderCreateRequest request, HttpSession session) {
        BasketResponse basket = basketService.getBasket(session);
        if (basket == null || basket.getItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다.");
        }

        Store store = em.getReference(Store.class, request.getStoreId());
        Kiosk kiosk = (request.getKioskId() != null) ? em.getReference(Kiosk.class, request.getKioskId()) : null;

        User user = null;
        
        // 장바구니 총 금액 계산
        int totalPrice = 0;
        for (BasketAddRequest basketItem : basket.getItems()) {
            totalPrice += basketItem.getUnitPrice() * basketItem.getQuantity();
        }

        // 전화번호가 넘어왔다면 유저 맵핑 (포인트 로직은 processPayment로 이관)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
            Optional<User> optionalUser = userRepository.findByPhone(request.getPhoneNumber());
            
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            } else {
                user = User.builder()
                        .phone(request.getPhoneNumber())
                        .pointBalance(0)
                        .build();
                userRepository.save(user);
            }
        } else if (request.getUserId() != null) {
            // 예외적으로 기존처럼 userId가 직접 넘어온 경우
            user = em.getReference(User.class, request.getUserId());
        }

        // 데이터베이스에서 가장 높은 주문 번호를 가져와서 1을 더함 (없으면 1부터 시작)
        int maxOrderNumber = orderRepository.findMaxOrderNumber();
        int nextOrderNumber = maxOrderNumber + 1;

        Order order = Order.builder()
                .store(store)
                .kiosk(kiosk)
                .user(user)
                .orderNumber(nextOrderNumber)
                .orderType(OrderType.valueOf(request.getOrderType()))
                .dryIceCount(request.getDryIceCount())
                .dryIceMins(request.getDryIceMins())
                .orderStatus(OrderStatus.WAITING)
                .build();
        
        orderRepository.save(order); // 여기서 order.getId()가 생성됨
        
        
        for (BasketAddRequest basketItem : basket.getItems()) {
            Product product = em.getReference(Product.class, basketItem.getProductId());
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(basketItem.getQuantity())
                    .unitPrice(basketItem.getUnitPrice())
                    .build();

            orderItemRepository.save(orderItem);

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
        
     // 지점에 새 주문 알림 전송
        messagingTemplate.convertAndSend(
                "/topic/store/" + request.getStoreId(),
                "새 주문이 들어왔습니다."
        );

        // 장바구니 초기화는 결제 성공 시점(processPayment)으로 이동
        return order.getId(); // 생성된 PK(orderId)를 반환
    }

    /**
     * 결제 처리 및 재고 차감 (통합 로직)
     */
    @Transactional
    public void processPayment(int orderId, String paymentMethod, int pointUsed, HttpSession session) {
        // 1. 주문 조회
        OrderResponse orderRes = orderMapper.selectOrderWithDetails(orderId);
        if (orderRes == null) throw new RuntimeException("주문을 찾을 수 없습니다.");
        
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("주문 엔티티를 찾을 수 없습니다."));

        // 2. 재고 차감 로직
        for (OrderItemDTO item : orderRes.getOrderItems()) {
            int updatedRows = orderMapper.decreaseProductStock(item.getProductId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new RuntimeException("상품 [" + item.getProductName() + "] 재고 부족");
                // System.out.println("Warning: 상품 [" + item.getProductName() + "] 재고가 부족하거나 인벤토리에 없습니다. (결제는 진행됩니다)");
            }
        }

        // 3. 결제 내역 세팅
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod); // 그대로 적용
        int baseAmount = orderRes.getTotalPrice();
        int couponDiscount = 0;
        int finalAmount = baseAmount - couponDiscount - pointUsed;

        payment.setBaseAmount(baseAmount);
        payment.setCouponDiscount(couponDiscount);
        payment.setPointUsed(pointUsed);
        payment.setFinalAmount(finalAmount);
        payment.setPaymentStatus("PAID"); // 명시적으로 지정
        payment.setPaymentDate(java.time.LocalDateTime.now());

        orderMapper.insertPayment(payment);
        
        // 4. 유저 포인트 차감 및 5% 적립
        if (order.getUser() != null) {
            User user = order.getUser();
            if (pointUsed > 0) {
                // 사용한 포인트 차감
                user.addPoint(-pointUsed);
            }
            // 최종 결제 금액의 5% 적립
            int earnedPoints = (int)(finalAmount * 0.05);
            if (earnedPoints > 0) {
                user.addPoint(earnedPoints);
            }
            userRepository.save(user);
        }

        // 5. 장바구니 비우기 (결제와 재고 차감이 모두 성공한 직후에만 실행)
        if (session != null) {
            basketService.clearBasket(session);
        }
    }
    
    @Transactional
    public void cancelOrder(int orderId) {
        // 1. 주문 상태를 CANCELED로 업데이트
        int updatedRows = orderMapper.updateOrderStatus(orderId, "CANCELED");
        
        if (updatedRows == 0) {
            throw new RuntimeException("주문을 찾을 수 없거나 취소할 수 없는 상태입니다.");
        }
    }
}