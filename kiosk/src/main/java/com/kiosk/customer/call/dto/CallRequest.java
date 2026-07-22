package com.kiosk.customer.call.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallRequest {
    private Integer storeId;
    private Integer kioskNo; // 키오스크 식별 번호 또는 ID
    private String reason;   // 호출 사유 (예: "주문 화면 호출", "결제 오류")
}
