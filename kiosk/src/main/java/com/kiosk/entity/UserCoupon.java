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
    private User user;

    // Coupon 테이블과 ManyToOne 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(name = "is_used", columnDefinition = "BOOLEAN DEFAULT false")
    @Builder.Default
    private boolean isUsed = false;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    // ✨ 추가 추천 1: 발급일 필드
    @Column(name = "issued_date", nullable = false)
    @Builder.Default
    private LocalDate issuedDate = LocalDate.now();

    // 비즈니스 로직: 쿠폰 사용 처리
    public void useCoupon() {
        this.isUsed = true;
    }

    //  쿠폰 사용 취소 처리
    public void cancelCoupon() {
        this.isUsed = false;
    }
}