package com.kiosk.customer.order.dto; 

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {
    private Integer storeId;       // 지점 ID
    private Integer kioskId;       // 키오스크 기기 ID
    private Integer userId;        // 적립 회원 ID
    private String orderType;      // "HERE" 또는 "TOGO"
    private Integer dryIceMins;    // 드라이아이스 시간

    // =========================================================
    // 🚀 [여기에 추가!] 장바구니 상세 내역을 받을 리스트 변수 추가
    // =========================================================
    private List<ItemDto> items;

    // 상세 상품 정보를 담을 Inner Class (전부 Integer로 통일!)
    @Getter
    @Setter
    public static class ItemDto {
        private Integer productId;
        private Integer quantity;
        private Integer unitPrice;
        private List<OrderFlavorDto> flavors;
        private List<Integer> options;
    }

    // 맛 선택 정보를 담을 세부 Inner Class
    @Getter
    @Setter
    public static class OrderFlavorDto {
        private Integer flavorId;
        private Integer quantity; 
    }
}