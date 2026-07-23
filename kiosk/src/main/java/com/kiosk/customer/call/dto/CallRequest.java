package com.kiosk.customer.call.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] CallRequest
 *
 * <p>역할: 직원 호출 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallRequest {
    private Integer storeId;
    private Integer kioskNo; // 키오스크 식별 번호 또는 ID
    private String reason;   // 호출 사유 (예: "주문 화면 호출", "결제 오류")
}
