package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kiosk.headquarter.dto.coupon.CouponRequestDto;
import com.kiosk.headquarter.repository.HeadCouponMapper;
import com.kiosk.headquarter.service.HeadCouponService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/head/coupon")
public class HeadCouponController {

    @Autowired
    private HeadCouponMapper headCouponMapper;
    private final HeadCouponService headCouponService;

    // 1. 전체 쿠폰 목록 조회 (GET /head/coupon)
    @GetMapping
    public List<CouponRequestDto> getAllCoupons() {
        return headCouponMapper.findAllCoupons();
    }

    // 2. 새 쿠폰 등록 (POST /head/coupon/insert)
    @PostMapping
    public String insertCoupon(@RequestBody CouponRequestDto request) {
        headCouponService.createCoupon(request);
        return "Coupon inserted successfully";
    }

    // 3. 쿠폰 수정 (PUT /head/coupon) 
    @PutMapping
    public String updateCoupon(@RequestBody CouponRequestDto request) {
        headCouponService.updateCoupon(request);
        return "Coupon updated successfully";
    }

    // 4. 쿠폰 삭제 (DELETE /head/coupon/{couponId})
    @DeleteMapping("/{couponId}")
    public String deleteCoupon(@PathVariable("couponId") String couponId) {
        headCouponService.deleteCoupon(couponId);
        return "Coupon deleted successfully";
    }
    
    // 5. 특정 쿠폰 전체 유저에게 일괄 발급(뿌리기) API
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