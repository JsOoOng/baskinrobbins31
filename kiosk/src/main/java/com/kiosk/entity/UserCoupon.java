package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * [코드 흐름 안내] UserCoupon
 *
 * <p>역할: 쿠폰 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 USER_COUPON 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 user_id, coupon_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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