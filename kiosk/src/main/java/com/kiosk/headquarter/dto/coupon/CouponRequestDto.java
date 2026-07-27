package com.kiosk.headquarter.dto.coupon;

import java.time.LocalDateTime;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] CouponRequestDto
 *
 * <p>역할: 쿠폰 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequestDto {
    private String couponId;
    private String couponName;
    private int discountValue;
    private String discountType;
    private int durationDays;
    private Boolean isIssuedAll;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    /*
     * 쉬운주석: 알림 조회에서 실제 user_coupon.expiry_date를 담을 때만 사용한다.
     */
    private LocalDate expiryDate;
}
