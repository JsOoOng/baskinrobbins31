package com.kiosk.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.OrderStatus;
import com.kiosk.entity.enums.OrderType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] Order
 *
 * <p>역할: 주문 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 ORDERS 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 store_id, kiosk_id, user_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "ORDERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kiosk_id")
    private Kiosk kiosk; // 주문 접수 방식에 따라 null 가능 (예: 배달앱 등 확장을 고려)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 비회원 주문일 경우 null

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    @Column(name = "dry_ice_count")
    @Builder.Default
    private Integer dryIceCount = 0;

    @Column(name = "dry_ice_mins")
    @Builder.Default
    private Integer dryIceMins = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.WAITING;
    
    public void changeOrderStatus(OrderStatus orderStatus) {
    	this.orderStatus = orderStatus;
    }
    
    @OneToOne(
    	    mappedBy = "order",
    	    cascade = CascadeType.ALL,
    	    orphanRemoval = true,
    	    fetch = FetchType.LAZY
    	)
    	private Payment payment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // 주문 상세
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    public int getTotalPrice() {
        int sum = 0;
        for (OrderItem item : orderItems) {
            sum += item.getUnitPrice() * item.getQuantity();
        }
        return sum; // ✅ 실제 계산된 값을 반환
    }

    public int getProductDiscountAmount() {
        int discountSum = 0;
        for (OrderItem item : orderItems) {
            Product p = item.getProduct();
            if (p != null && p.getBasePrice() != null && p.getFinalPrice() != null) {
                int diff = p.getBasePrice() - p.getFinalPrice();
                discountSum += diff * item.getQuantity();
            }
        }
        return discountSum;
    }

    public int getOriginalBaseAmount() {
        return getTotalPrice() + getProductDiscountAmount();
    }
    
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderStatusHistory> orderStatusHistories = new ArrayList<>();
}