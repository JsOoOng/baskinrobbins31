package com.kiosk.headquarter.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.headquarter.dto.coupon.CouponRequestDto;

/**
 * [코드 흐름 안내] HeadCouponMapper
 *
 * <p>역할: 본사 관리의 쿠폰 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Mapper
public interface HeadCouponMapper {

    List<CouponRequestDto>
            findAllCoupons();

    List<CouponRequestDto>
            findCouponExpiryDates();

    void insertCoupon(
            CouponRequestDto request
    );

    void updateCoupon(
            CouponRequestDto request
    );

    void deleteUserCouponsByCouponId(
            @Param("couponId")
            String couponId
    );

    void deleteCoupon(
            @Param("couponId")
            String couponId
    );

    void insertCouponToAllUsers(
            @Param("couponId")
            String couponId
    );

    void updateCouponIssuedStatus(
            @Param("couponId")
            String couponId
    );

    boolean isCouponIssuedAll(
            @Param("couponId")
            String couponId
    );

    void resetCouponIssuedStatus(
            @Param("couponId")
            String couponId
    );
}
