package com.kiosk.customer.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class OrderCreateRequest {
    
    private Integer storeId;       // 주문 발생 지점 [cite: 21]
    private Integer kioskId;       // 주문 발생 키오스크 기기 [cite: 21]
    private Integer userId;        // 회원 식별자 (비회원은 null) [cite: 22]
    private String orderType;      // HERE(먹고가기) / TOGO(포장하기) [cite: 23]
    private Integer dryIceMins;    // 드라이아이스 제공 시간 [cite: 23]
    private Integer totalPrice;    // 총 주문 금액 [cite: 24]
    private List<ItemDto> items;   // 상세 주문 항목 리스트

    @Getter
    @NoArgsConstructor
    public static class ItemDto {
        private Integer productId;        // 구매한 상품 ID [cite: 25]
        private Integer quantity;         // 구매 수량 [cite: 26]
        private Integer unitPrice;        // 반영된 단가 [cite: 26]
        private List<FlavorDto> flavors;  // 선택한 아이스크림 맛 리스트
        private List<Integer> options;    // 선택한 옵션 ID 리스트 (301, 302 등)
    }

    @Getter
    @NoArgsConstructor
    public static class FlavorDto {
        private Integer flavorId;  // 선택한 맛 ID [cite: 28]
        private Integer quantity;  // 해당 맛을 담은 개수 (예: 민트초코 더블로 담기) [cite: 28, 29]
    }
}