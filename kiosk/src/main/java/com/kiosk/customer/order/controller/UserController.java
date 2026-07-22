package com.kiosk.customer.order.controller;

import com.kiosk.customer.order.dto.UserPointResponseDto;
import com.kiosk.customer.order.repository.UserCouponRepository;
import com.kiosk.customer.order.repository.UserRepository;
import com.kiosk.entity.User;
import com.kiosk.entity.UserCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;

    @GetMapping("/points")
    public ResponseEntity<?> getUserPoints(@RequestParam(value = "phone") String phone) {
        try {
            System.out.println(">>> 받은 전화번호 파라미터: " + phone);

            User user = userRepository.findByPhoneIgnoringHyphen(phone)
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .phone(phone)
                                .pointBalance(0)
                                .build();
                        return userRepository.save(newUser);
                    });

            List<UserCoupon> userCoupons = userCouponRepository.findByUserId(user.getId());
            if (userCoupons == null) {
                userCoupons = Collections.emptyList();
            }

            List<UserPointResponseDto.CouponDto> couponDtos = userCoupons.stream()
                    .map(uc -> new UserPointResponseDto.CouponDto(
                            uc.getUserCouponId(),
                            uc.getCoupon() != null ? uc.getCoupon().getCouponName() : "쿠폰",
                            uc.getCoupon() != null ? uc.getCoupon().getDiscountValue() : 0,
                            uc.getCoupon() != null ? uc.getCoupon().getDiscountType() : "AMOUNT",
                            uc.isUsed(),
                            uc.getExpiryDate() // [추가] user_coupon의 만료일자 매핑
                    ))
                    .collect(Collectors.toList());

            UserPointResponseDto response = UserPointResponseDto.builder()
                    .pointBalance(user.getPointBalance() != null ? user.getPointBalance() : 0)
                    .coupons(couponDtos)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 어떤 에러 때문에 400(또는 내부 오류)이 나는지 콘솔에 상세히 출력
            e.printStackTrace();
            return ResponseEntity.badRequest().body("에러 발생 원인: " + e.getMessage());
        }
    }
}