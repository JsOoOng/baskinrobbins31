package com.kiosk.headquarter.dto.product;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadProductCreateRequestDTO {

    /*
     * Category 엔티티와 Repository의 ID 타입이
     * Integer이므로 Integer를 사용합니다.
     */
    private Integer categoryId;

    private String productName;

    private String description;

    private Integer basePrice;

    private Integer discountRate;

    // 입력하지 않으면 서비스에서 기본값 65 사용
    private Integer marginRate;

    private Boolean isDisplay;

    private String imageUrl;
    
    private String newFlavorName;
    
    private String newFlavorImageUrl;

    /*
     * 상품을 판매할 지점 ID 목록
     *
     * 프론트 요청 예:
     * "storeIds": [1, 2, 3]
     */
    @Builder.Default
    private List<Integer> storeIds =
            new ArrayList<>();

    // INVENTORY_ITEMS 데이터
    private String inventoryItemName;
    private String inventoryUnit;
    private Integer inventoryUnitPrice;
}