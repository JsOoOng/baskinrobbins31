package com.kiosk.headquarter.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.headquarter.dto.coupon.CouponRequestDto;

@Mapper
public interface HeadCouponMapper {

    List<CouponRequestDto>
            findAllCoupons();

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
}