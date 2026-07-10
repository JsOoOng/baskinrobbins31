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
import com.kiosk.customer.order.dto.Payment;
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
    private final EntityManager em;

    private final OrderItemMapper orderItemRepository;
    private final OrderItemFlavorMapper orderItemFlavorRepository;
    private final OrderItemOptionMapper orderItemOptionRepository;
    private final OrderRepository orderRepository;
    
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
    public void processPayment(int orderId, String paymentMethod) {
        // 1. 주문 조회
        OrderResponse orderRes = orderMapper.selectOrderWithDetails(orderId);
        if (orderRes == null) throw new RuntimeException("주문을 찾을 수 없습니다.");

        // 2. 재고 차감 로직
        for (OrderItemDTO item : orderRes.getOrderItems()) {
            int updatedRows = orderMapper.decreaseProductStock(item.getProductId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new RuntimeException("상품 [" + item.getProductName() + "] 재고 부족");
            }
        }

        // 3. 결제 내역 저장
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(paymentMethod); // 그대로 사용
        payment.setBaseAmount(orderRes.getTotalPrice());
        payment.setFinalAmount(orderRes.getTotalPrice());
        payment.setPaymentStatus("PAID"); // 명시적으로 설정

        orderMapper.insertPayment(payment);

        // 4. 주문 상태 업데이트
        orderMapper.updateOrderStatus(orderId, "COMPLETED");
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