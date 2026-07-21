package com.kiosk.customer.order.service;

import java.time.LocalDateTime;

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
import com.kiosk.customer.order.repository.UserCouponRepository;
import com.kiosk.customer.order.repository.UserRepository;
import com.kiosk.entity.Coupon;
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
import com.kiosk.entity.UserCoupon;
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
    
    private final UserCouponRepository userCouponRepository;
    
    
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
        	Optional<User> optionalUser = userRepository.findByPhoneIgnoringHyphen(request.getPhoneNumber());
            
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
    public void processPayment(int orderId, String paymentMethod, int userCouponId, int pointUsed, HttpSession session) {
        
        // 1. 주문 조회 (엔티티 기반)
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 엔티티를 찾을 수 없습니다."));

        int discountedAmount = order.getTotalPrice(); // 상품 할인만 적용된 총 금액 (프론트에서 보낸 단가 합산)
        int productDiscount = order.getProductDiscountAmount(); // 상품 자체 할인 총액 계산
        int baseAmount = order.getOriginalBaseAmount(); // 결제 전 원본 총액 (basePrice 기준)
        
        int finalAmount = discountedAmount;
        int couponDiscount = 0;

     // 2. 쿠폰 계산 및 적용 부분에 로그 추가
        System.out.println(">>> 전달받은 userCouponId: " + userCouponId);

        if (userCouponId > 0) {
            System.out.println(">>> 쿠폰 사용 로직 진입 성공!");
            UserCoupon uc = userCouponRepository.findById(userCouponId)
                    .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다."));
            
            couponDiscount = calculateDiscount(finalAmount, uc.getCoupon());
            System.out.println("쿠폰 할인액: " + couponDiscount);
            
            if (finalAmount - couponDiscount < 0) {
                System.out.println(">>> [예외 발생] 쿠폰 할인 금액이 결제 총액을 초과했습니다.");
                throw new IllegalArgumentException("상품 금액보다 할인 쿠폰 금액이 커서 사용할 수 없습니다.");
            }
            
            finalAmount -= couponDiscount;
            uc.useCoupon(); 
            userCouponRepository.save(uc);
        } else {
            System.out.println(">>> userCouponId가 0이거나 전달되지 않아 쿠폰 로직을 타지 않습니다.");
        }

        // 3. 포인트 사용 적용 및 5% 적립 (dev1 + feature 혼합)
        User user = order.getUser(); 
        if (user != null) {
            // 3-1. 포인트 사용
            if (pointUsed > 0) {
                // 결제 금액을 초과해서 포인트를 사용할 수 없도록 방어
                pointUsed = Math.min(pointUsed, finalAmount);
                finalAmount -= pointUsed;
                
                // 포인트 차감 (참고: User 엔티티에 deductPoints가 있다면 그걸 쓰셔도 무방합니다)
                user.addPoint(-pointUsed); 
            }

            // 3-2. 포인트 적립 (최종 결제 금액이 0보다 클 때만)
            if (finalAmount > 0) {
                int earnedPoints = (int)(finalAmount * 0.05);
                if (earnedPoints > 0) {
                    user.addPoint(earnedPoints);
                }
            }
            userRepository.save(user);
        }

        // 4. 재고 차감
        for (OrderItem item : order.getOrderItems()) {
            int updatedRows = orderMapper.decreaseProductStock(item.getProduct().getId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new RuntimeException("상품 [" + item.getProduct().getProductName() + "] 재고 부족");
            }
        }

        int totalDiscount = productDiscount + couponDiscount + pointUsed;

        // 5. 결제 정보 저장 (Payments 테이블에 INSERT)
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod);
        payment.setBaseAmount(baseAmount);
        payment.setProductDiscount(productDiscount);
        payment.setCouponDiscount(couponDiscount);
        payment.setPointUsed(pointUsed);
        payment.setTotalDiscount(totalDiscount);
        payment.setFinalAmount(finalAmount);
        payment.setPaymentStatus("PAID");
        payment.setPaymentDate(LocalDateTime.now());
        orderMapper.insertPayment(payment);
        
        // 6. 주문 상태 업데이트 (ORDERS 테이블 상태 변경)
        orderMapper.updateOrderStatus(orderId, "COMPLETED");

        // 7. 장바구니 비우기 (결제 완료 후 세션 초기화)
        if (session != null) {
            basketService.clearBasket(session);
        }
    }
    
    // 결제 방법
    private int calculateDiscount(int originalPrice, Coupon coupon) {
        String discountType = coupon.getDiscountType(); 
        int discountValue = coupon.getDiscountValue();  

        if ("PERCENT".equals(discountType)) {
            return (int) (originalPrice * (discountValue / 100.0));
        } else if ("AMOUNT".equals(discountType)) { // AMOUNT만 처리
            return discountValue;
        }
        
        // 만약 정의되지 않은 타입이 들어올 경우를 대비해 0 반환
        return 0;
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