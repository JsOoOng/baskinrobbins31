package com.kiosk.headquarter.repository;

import java.util.List; // 1. List 임포트 추가
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.kiosk.headquarter.dto.coupon.CouponRequestDto;

@Mapper
public interface HeadCouponMapper {
   
    List<CouponRequestDto> findAllCoupons();
    void insertCoupon(CouponRequestDto request);
    void updateCoupon(CouponRequestDto request);
    void deleteUserCouponsByCouponId(String couponId);
    void deleteCoupon(@Param("couponId") String couponId);
    void insertCouponToAllUsers(@Param("couponId") String couponId);
    void updateCouponIssuedStatus(@Param("couponId") String couponId);
}
