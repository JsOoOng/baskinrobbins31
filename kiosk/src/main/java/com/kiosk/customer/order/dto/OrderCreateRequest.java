package com.kiosk.customer.order.dto; 

import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * [코드 흐름 안내] OrderCreateRequest
 *
 * <p>역할: 주문 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
public class OrderCreateRequest {
    private Integer storeId;       // 지점 ID
    private Integer kioskId;       // 키오스크 기기 ID
    private Integer userId;        // 적립 회원 ID
    private String orderType;      // "HERE" 또는 "TOGO"
    private Integer dryIceCount;   // 드라이아이스 개수
    private Integer dryIceMins;    // 포장 소요 시간
    
    // 포인트 적립용 회원 전화번호 (없는 경우 null 또는 빈 문자열)
    private String phoneNumber;

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