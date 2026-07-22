package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime; // 임포트 추가

@Entity
@Table(name = "Coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @Column(name = "coupon_id", length = 20)
    private String couponId;

    @Column(name = "coupon_name", length = 100, nullable = false)
    private String couponName;

    @Column(name = "discount_value", nullable = false)
    private Integer discountValue;

    @Column(name = "discount_type", length = 10, nullable = false)
    private String discountType; // 'PERCENT' 또는 'AMOUNT'

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;
    
    @Column(name = "is_issued_all", nullable = false)
    @Builder.Default
    private Boolean isIssuedAll = false;

    // [추가] 쿠폰 등록 시간 타임스탬프
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 데이터가 처음 저장될 때 자동으로 현재 시간 입력
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
    
    public void updateIssuedStatus(boolean isIssuedAll) {
        this.isIssuedAll = isIssuedAll;
    }
}