package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "COUPON")
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
    private int discountValue;

    @Column(name = "discount_type", length = 10, nullable = false)
    private String discountType; // 'PERCENT' 또는 'AMOUNT'

    @Column(name = "duration_days", nullable = false)
    private int durationDays;
}
