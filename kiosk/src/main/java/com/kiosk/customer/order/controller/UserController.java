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

/**
 * [코드 흐름 안내] UserController
 *
 * <p>역할: 고객 키오스크의 회원 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/users) -> UserRepository, UserCouponRepository -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserCouponRepository userCouponRepository;

    /**
     * [요청 흐름] GET /api/users/points
     * 프론트 요청을 받아 getUserPoints() 메서드가 입력을 받고 UserRepository, UserCouponRepository 호출 후 결과를 응답한다.
     */
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