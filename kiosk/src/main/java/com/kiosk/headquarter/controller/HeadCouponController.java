package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kiosk.headquarter.dto.coupon.CouponRequestDto;
import com.kiosk.headquarter.repository.HeadCouponMapper;
import com.kiosk.headquarter.service.HeadCouponService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadCouponController
 *
 * <p>역할: 본사 관리의 쿠폰 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/coupon) -> HeadCouponService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/head/coupon")
public class HeadCouponController {

    @Autowired
    private HeadCouponMapper headCouponMapper;
    private final HeadCouponService headCouponService;

    // 1. 전체 쿠폰 목록 조회 (GET /head/coupon)
    /**
     * [요청 흐름] GET /head/coupon
     * 프론트 요청을 받아 getAllCoupons() 메서드가 입력을 받고 HeadCouponService 호출 후 결과를 응답한다.
     */
    @GetMapping
    public List<CouponRequestDto> getAllCoupons() {
        return headCouponMapper.findAllCoupons();
    }

    // 2. 새 쿠폰 등록 (POST /head/coupon/insert)
    /**
     * [요청 흐름] POST /head/coupon
     * 프론트 요청을 받아 insertCoupon() 메서드가 입력을 받고 HeadCouponService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public String insertCoupon(@RequestBody CouponRequestDto request) {
        headCouponService.createCoupon(request);
        return "Coupon inserted successfully";
    }

    // 3. 쿠폰 수정 (PUT /head/coupon) 
    /**
     * [요청 흐름] PUT /head/coupon
     * 프론트 요청을 받아 updateCoupon() 메서드가 입력을 받고 HeadCouponService 호출 후 결과를 응답한다.
     */
    @PutMapping
    public String updateCoupon(@RequestBody CouponRequestDto request) {
        headCouponService.updateCoupon(request);
        return "Coupon updated successfully";
    }

    // 4. 쿠폰 삭제 (DELETE /head/coupon/{couponId})
    /**
     * [요청 흐름] DELETE /head/coupon/{couponId}
     * 프론트 요청을 받아 deleteCoupon() 메서드가 입력을 받고 HeadCouponService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/{couponId}")
    public String deleteCoupon(@PathVariable("couponId") String couponId) {
        headCouponService.deleteCoupon(couponId);
        return "Coupon deleted successfully";
    }
    
    // 5. 특정 쿠폰 전체 유저에게 일괄 발급(뿌리기) API
    /**
     * [요청 흐름] POST /head/coupon/issue-all/{couponId}
     * 프론트 요청을 받아 issueCouponToAllUsers() 메서드가 입력을 받고 HeadCouponService 호출 후 결과를 응답한다.
     */
    @PostMapping("/issue-all/{couponId}")
    public ResponseEntity<String> issueCouponToAllUsers(@PathVariable("couponId") String couponId) {
        try {
            headCouponService.issueCouponToAllUsers(couponId);
            return ResponseEntity.ok("쿠폰이 모든 유저에게 성공적으로 발급되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("쿠폰 일괄 발급 실패: " + e.getMessage());
        }
    }
}