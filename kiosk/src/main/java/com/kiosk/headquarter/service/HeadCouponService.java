package com.kiosk.headquarter.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kiosk.headquarter.repository.HeadCouponMapper;
import com.kiosk.entity.enums.NotificationCategory;
import com.kiosk.entity.enums.NotificationType;
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
    private final HeadNotificationService headNotificationService;

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

    /**
     * 쉬운주석: 전체 발급된 쿠폰만 회수할 수 있다.
     * 회원별 쿠폰을 지운 뒤 발급 상태를 X로 되돌려 수정과 재발급이 가능하게 한다.
     */
    @Transactional
    public void revokeCouponFromAllUsers(String couponId) {
        if (!headCouponMapper.isCouponIssuedAll(couponId)) {
            throw new IllegalStateException("전체 발급된 쿠폰만 회수할 수 있습니다.");
        }

        headCouponMapper.deleteUserCouponsByCouponId(couponId);
        headCouponMapper.resetCouponIssuedStatus(couponId);
        adminLogService.logAction("쿠폰", "쿠폰 전체 회수 (ID: " + couponId + ")");
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

    /**
     * 쉬운주석: 실제 발급된 회원 쿠폰의 expiry_date로 남은 날짜를 계산한다.
     * 매시간 실행되며 7일·1일 전과 만료 시점에 각각 한 번만 알림을 만든다.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void notifyCouponPeriods() {
        LocalDate today = LocalDate.now();

        for (com.kiosk.headquarter.dto.coupon.CouponRequestDto coupon
                : headCouponMapper.findCouponExpiryDates()) {
            if (coupon.getExpiryDate() == null) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(
                    today,
                    coupon.getExpiryDate()
            );
            String reference = coupon.getCouponId() + ":" + coupon.getExpiryDate();

            if (days == 7 || days == 1) {
                String remaining = days == 1 ? "하루" : "일주일";
                headNotificationService.createNotificationOnce(
                        NotificationCategory.COUPON,
                        NotificationType.COUPON_EXPIRING,
                        "쿠폰 만료 " + remaining + " 전",
                        coupon.getCouponName() + " 쿠폰이 " + remaining + " 남았습니다.",
                        "head-coupons",
                        reference
                );
            }
            if (!coupon.getExpiryDate().isAfter(today)) {
                headNotificationService.createNotificationOnce(
                        NotificationCategory.COUPON,
                        NotificationType.COUPON_EXPIRED,
                        "쿠폰 만료",
                        coupon.getCouponName() + " 쿠폰이 만료되었습니다.",
                        "head-coupons",
                        reference
                );
            }
        }
    }
}
