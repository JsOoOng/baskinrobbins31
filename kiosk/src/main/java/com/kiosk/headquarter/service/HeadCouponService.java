package com.kiosk.headquarter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kiosk.headquarter.repository.HeadCouponMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HeadCouponService {

    private final HeadCouponMapper headCouponMapper;

    @Transactional
    public void issueCouponToAllUsers(String couponId) {
        // 1. 모든 유저에게 쿠폰 발급 (INSERT INTO user_coupon ...)
        headCouponMapper.insertCouponToAllUsers(couponId);
        
        // 2. 쿠폰 테이블의 뿌리기 상태를 O(true)로 변경 (UPDATE coupon SET is_issued_all = TRUE ...)
        headCouponMapper.updateCouponIssuedStatus(couponId);
    }
    
    @Transactional
    public void deleteCoupon(String couponId) {
        // 1. 외래 키로 묶여 있는 user_coupon 데이터 먼저 삭제
        headCouponMapper.deleteUserCouponsByCouponId(couponId);
        
        // 2. 본사 쿠폰 테이블에서 쿠폰 삭제
        headCouponMapper.deleteCoupon(couponId);
    }
}