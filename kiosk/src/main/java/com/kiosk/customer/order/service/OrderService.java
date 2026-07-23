package com.kiosk.customer.order.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import com.kiosk.customer.basket.dto.BasketAddRequest;
import com.kiosk.customer.basket.dto.BasketResponse;
import com.kiosk.customer.basket.service.BasketService;
import com.kiosk.customer.order.dto.OrderCreateRequest;
import com.kiosk.customer.order.dto.OrderCreateRequest.OrderFlavorDto;
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

import java.util.ArrayList;
import java.util.List;
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
        // 1. 프론트엔드에서 보낸 아이템 목록이 비어있는지 검증
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어 있습니다.");
        }

        Kiosk kiosk = (request.getKioskId() != null) ? em.find(Kiosk.class, request.getKioskId()) : null;
        if (kiosk == null) {
            throw new IllegalArgumentException("존재하지 않는 키오스크입니다.");
        }
        Store store = kiosk.getStore();
        if (store == null) {
            throw new IllegalArgumentException("키오스크에 연결된 지점이 없습니다.");
        }

        // ==========================================
        // 2. 주문 생성 직전, 프론트엔드가 보낸 상품들의 재고 부족 검증 수행
        // ==========================================
        List<String> outOfStockItems = new ArrayList<>();

        for (OrderCreateRequest.ItemDto itemDto : request.getItems()) {
            int currentStock = orderMapper.selectProductStock(itemDto.getProductId());
            
            if (currentStock - itemDto.getQuantity() < 0) {
                Product product = em.find(Product.class, itemDto.getProductId());
                String productName = (product != null) ? product.getProductName() : "상품(ID:" + itemDto.getProductId() + ")";
                
                String msg = String.format("• %s (주문: %d개 / 잔여: %d개)", 
                                           productName, 
                                           itemDto.getQuantity(), 
                                           currentStock);
                outOfStockItems.add(msg);
            }
        }

        // 재고가 부족한 상품이 하나라도 있으면 즉시 예외 발생 (주문 생성 안 됨)
        if (!outOfStockItems.isEmpty()) {
            String errorMessage = "재고가 부족한 상품이 있습니다:\n\n" + String.join("\n", outOfStockItems);
            throw new RuntimeException(errorMessage);
        }
        // ==========================================

        User user = null;
        
        // 총 금액 계산 (프론트엔드가 보낸 itemDto 기준)
        int totalPrice = 0;
        for (OrderCreateRequest.ItemDto itemDto : request.getItems()) {
            totalPrice += itemDto.getUnitPrice() * itemDto.getQuantity();
        }

        // 전화번호가 넘어왔다면 유저 맵핑
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
            user = em.getReference(User.class, request.getUserId());
        }

        // 지점별 및 오늘 날짜 기준으로 가장 높은 주문 번호 가져오기
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        int maxOrderNumber = orderRepository.findMaxOrderNumberByStoreAndDate(store.getId(), startOfDay);
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
        
        orderRepository.save(order); // orderId 생성
        
        // 프론트엔드가 보낸 아이템 리스트를 반복돌며 DB에 저장
        for (OrderCreateRequest.ItemDto itemDto : request.getItems()) {
            Product product = em.getReference(Product.class, itemDto.getProductId());
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .build();

            orderItemRepository.save(orderItem);

            // 맛(flavors) 저장
            if (itemDto.getFlavors() != null) {
                for (OrderFlavorDto flavorDto : itemDto.getFlavors()) {
                    IcecreamFlavor flavor = em.getReference(IcecreamFlavor.class, flavorDto.getFlavorId());
                    OrderItemFlavor itemFlavor = OrderItemFlavor.builder()
                            .orderItem(orderItem)
                            .flavor(flavor)
                            .quantity(flavorDto.getQuantity())
                            .build();
                    orderItemFlavorRepository.save(itemFlavor);
                }
            }

            // 옵션(options) 저장
            if (itemDto.getOptions() != null) {
                for (Integer optionId : itemDto.getOptions()) {
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
                "/topic/store/" + store.getId(),
                "새 주문이 들어왔습니다."
        );

        return order.getId();
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

     // 4. 재고 부족 검증 (여러 개일 경우 모두 모아서 한 번에 메시지 생성)
        List<String> outOfStockItems = new ArrayList<>();

        for (OrderItem item : order.getOrderItems()) {
            int currentStock = orderMapper.selectProductStock(item.getProduct().getId());
            
            if (currentStock - item.getQuantity() < 0) {
                // 부족한 상품명과 재고 상태를 리스트에 담음
                String msg = String.format("• %s (주문: %d개 / 잔여: %d개)", 
                                           item.getProduct().getProductName(), 
                                           item.getQuantity(), 
                                           currentStock);
                outOfStockItems.add(msg);
            }
        }

        // 부족한 상품이 하나라도 존재한다면
        if (!outOfStockItems.isEmpty()) {
            // 키오스크 화면에 보기 좋게 줄바꿈(\n)으로 합쳐서 한 번에 던짐
            String errorMessage = "재고가 부족한 상품이 있습니다:\n\n" + String.join("\n", outOfStockItems);
            throw new RuntimeException(errorMessage);
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
        orderMapper.updateOrderStatus(orderId, "WAITING");

        // 7. 장바구니 비우기 (결제 완료 후 세션 초기화)
        if (session != null) {
            basketService.clearBasket(session);
        }
    }
    
    // 쿠폰 사용
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