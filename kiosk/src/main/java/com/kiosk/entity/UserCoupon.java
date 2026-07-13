package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "USER_COUPON")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Integer userCouponId;

    // USERS 테이블과 ManyToOne 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // USERS 테이블의 Entity 클래스명에 맞춰주세요

    // Coupon 테이블과 ManyToOne 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "is_used", columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isUsed = false;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    // 비즈니스 로직: 쿠폰 사용 처리
    public void useCoupon() {
        this.isUsed = true;
    }
}
