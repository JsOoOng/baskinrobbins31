package com.kiosk.customer.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPointResponseDto {
    private Integer pointBalance;
    private List<CouponDto> coupons;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CouponDto {
        private Integer userCouponId;
        private String couponName;
        private Integer discountValue;
        private String discountType;
        private Boolean isUsed;
    }
}