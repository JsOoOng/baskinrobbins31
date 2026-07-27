package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.PaymentMethod;
import com.kiosk.entity.enums.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] Payment
 *
 * <p>역할: 결제 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 PAYMENTS 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 order_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "PAYMENTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "base_amount", nullable = false)
    private Integer baseAmount;

    @Column(name = "product_discount", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer productDiscount = 0; // 상품 자체 할인 적용 금액 총합

    @Column(name = "coupon_discount", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer couponDiscount = 0;

    @Column(name = "point_used", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer pointUsed = 0;

    @Column(name = "total_discount", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer totalDiscount = 0; // 총 할인 금액 (product + coupon + point)

    @Column(name = "final_amount", nullable = false)
    private Integer finalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PAID;

    @CreationTimestamp
    @Column(name = "payment_date", updatable = false)
    private LocalDateTime paymentDate;
}