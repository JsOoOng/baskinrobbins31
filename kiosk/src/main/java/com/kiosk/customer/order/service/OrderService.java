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
        User user = (request.getUserId() != null) ? em.getReference(User.class, request.getUserId()) : null;

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

        basketService.clearBasket(session);
        return order.getId(); // 생성된 PK(orderId)를 반환
    }

    /**
     * 결제 처리 및 재고 차감 (통합 로직)
     */
    @Transactional
    public void processPayment(int orderId, String paymentMethod, int userCouponId, boolean usePoints) {
    	
    	// 1. 주문 조회
    	Order order = orderRepository.findById(orderId)
    	        .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

    	int finalAmount = order.getTotalPrice();

    	// 2. 쿠폰 계산 (먼저 할인 적용)
    	if (userCouponId > 0) {
    	    UserCoupon uc = userCouponRepository.findById(userCouponId)
    	        .orElseThrow(() -> new RuntimeException("쿠폰 없음"));
    	    
    	    int discount = calculateDiscount(finalAmount, uc.getCoupon());
    	    System.out.println("쿠폰 할인액: " + discount); // <--- 이 로그를 찍어보세요!
    	    finalAmount -= discount;
    	    uc.useCoupon(); 
    	}

    	// 3. 포인트 로직 (쿠폰 할인 후 남은 금액만큼만 사용)
    	int pointsToUse = 0;
    	User user = order.getUser(); 
    	if (user != null && usePoints) {
    	    // 결제 금액보다 포인트가 많으면 결제 금액만큼만, 적으면 가진 포인트만큼만 사용
    	    pointsToUse = Math.min(user.getPointBalance(), finalAmount); 
    	    
    	    finalAmount -= pointsToUse;
    	    user.deductPoints(pointsToUse);
    	}

    	// 4. 포인트 적립 (0보다 클 때만)
    	if (user != null && finalAmount > 0) {
    	    user.addPoints((int)(finalAmount * 0.05));
    	}

        // 4. 재고 차감
        for (OrderItem item : order.getOrderItems()) {
            int updatedRows = orderMapper.decreaseProductStock(item.getProduct().getId(), item.getQuantity());
            if (updatedRows == 0) throw new RuntimeException("재고 부족: " + item.getProduct().getProductName());
        }

        // 5. 결제 정보 저장 (Payments 테이블에 INSERT)
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setBaseAmount(order.getTotalPrice());
        payment.setFinalAmount(finalAmount);
        payment.setPointUsed(usePoints ? pointsToUse : 0);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus("PAID");
        orderMapper.insertPayment(payment);
        
        // 6. 주문 상태 업데이트 (ORDERS 테이블에는 상태만 변경!)
        // 기존의 updatePaymentStatus 대신 상태만 변경하는 메서드 사용
        orderMapper.updateOrderStatus(orderId, "COMPLETED");
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