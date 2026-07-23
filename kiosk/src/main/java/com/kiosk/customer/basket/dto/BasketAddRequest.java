package com.kiosk.customer.basket.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * [코드 흐름 안내] BasketAddRequest
 *
 * <p>역할: 장바구니 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
public class BasketAddRequest {
    private Integer productId;        // 상품 ID (예: 하프갤런)
    private String productName;       // 상품 이름 추가
    private Integer quantity;         // 수량
    private Integer unitPrice;        // 단가
    private List<FlavorDto> flavors;  // 선택한 맛 목록
    private List<Integer> options;    // 선택한 옵션 ID 목록 (스푼 등)
    private Boolean extraSpoons;      // 스푼 추가 여부 (프론트 UI 용)

    @Getter
    @Setter
    public static class FlavorDto {
        private Integer flavorId;
        private String flavorName;    // 맛 이름 추가
        private Integer quantity;
    }
}