package com.kiosk.customer.order.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * [코드 흐름 안내] Payment
 *
 * <p>역할: 결제 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Data
public class Payment {
    private int paymentId;        // 결제 고유 번호 (PK)
    private int orderId;          // 주문 번호 (FK)
    private String paymentMethod; // 결제 수단 (CARD, CASH, E_PAY, COUPON)
    private int baseAmount;       // 상품 기본 총 금액 (할인 전)
    private int productDiscount;  // 상품 자체 할인 총액
    private int couponDiscount;   // 쿠폰 할인 금액
    private int pointUsed;        // 포인트 사용 금액
    private int totalDiscount;    // 총 할인 금액 (상품 + 쿠폰 + 포인트)
    private int finalAmount;      // 최종 결제 금액
    private String paymentStatus; // 결제 상태
    private LocalDateTime paymentDate; // 결제 일시
}
