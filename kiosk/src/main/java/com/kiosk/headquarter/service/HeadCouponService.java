package com.kiosk.headquarter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kiosk.headquarter.repository.HeadCouponMapper;
import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadCouponService
 *
 * <p>역할: 본사 관리의 쿠폰 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadCouponMapper, AdminLogService -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
public class HeadCouponService {

    private final HeadCouponMapper headCouponMapper;
    private final AdminLogService adminLogService;

    @Transactional
    /**
     * [메서드 흐름] createCoupon
     * Controller 또는 상위 서비스에서 호출되어 HeadCouponMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void createCoupon(com.kiosk.headquarter.dto.coupon.CouponRequestDto request) {
        headCouponMapper.insertCoupon(request);
        adminLogService.logAction("쿠폰", request.getCouponName() + " 쿠폰 신규 등록");
    }

    @Transactional
    /**
     * [메서드 흐름] updateCoupon
     * Controller 또는 상위 서비스에서 호출되어 HeadCouponMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void updateCoupon(com.kiosk.headquarter.dto.coupon.CouponRequestDto request) {
        headCouponMapper.updateCoupon(request);
        adminLogService.logAction("쿠폰", request.getCouponName() + " 쿠폰 정보 수정");
    }

    @Transactional
    /**
     * [메서드 흐름] issueCouponToAllUsers
     * Controller 또는 상위 서비스에서 호출되어 HeadCouponMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void issueCouponToAllUsers(String couponId) {
        // 1. 모든 유저에게 쿠폰 발급 (INSERT INTO user_coupon ...)
        headCouponMapper.insertCouponToAllUsers(couponId);
        
        // 2. 쿠폰 테이블의 뿌리기 상태를 O(true)로 변경 (UPDATE coupon SET is_issued_all = TRUE ...)
        headCouponMapper.updateCouponIssuedStatus(couponId);
        
        adminLogService.logAction("쿠폰", "쿠폰 전체 일괄 발급 (ID: " + couponId + ")");
    }
    
    @Transactional
    /**
     * [메서드 흐름] deleteCoupon
     * Controller 또는 상위 서비스에서 호출되어 HeadCouponMapper, AdminLogService을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void deleteCoupon(String couponId) {
        // 1. 외래 키로 묶여 있는 user_coupon 데이터 먼저 삭제
        headCouponMapper.deleteUserCouponsByCouponId(couponId);
        
        // 2. 본사 쿠폰 테이블에서 쿠폰 삭제
        headCouponMapper.deleteCoupon(couponId);
        
        adminLogService.logAction("쿠폰", "쿠폰 삭제 (ID: " + couponId + ")");
    }
}