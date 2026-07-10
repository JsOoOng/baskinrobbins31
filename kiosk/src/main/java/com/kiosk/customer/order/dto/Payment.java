package com.kiosk.customer.order.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Payment {
    private int paymentId;        // 결제 고유 번호 (PK)
    private int orderId;          // 주문 번호 (FK)
    private String paymentMethod; // 결제 수단 (CARD, CASH, E_PAY, COUPON)
    private int baseAmount;       // 상품 원본 총 금액
    private int couponDiscount;   // 쿠폰 할인 금액
    private int pointUsed;        // 포인트 사용 금액
    private int finalAmount;      // 최종 결제 금액
    private String paymentStatus; // 결제 상태
    private LocalDateTime paymentDate; // 결제 일시
}
