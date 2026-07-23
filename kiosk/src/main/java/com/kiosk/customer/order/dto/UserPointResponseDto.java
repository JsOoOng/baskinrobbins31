package com.kiosk.customer.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * [코드 흐름 안내] UserPointResponseDto
 *
 * <p>역할: 회원 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
        private LocalDate expiryDate;
    }
}