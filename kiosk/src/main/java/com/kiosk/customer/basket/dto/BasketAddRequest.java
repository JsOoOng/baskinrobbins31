package com.kiosk.customer.basket.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

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