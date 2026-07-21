package com.kiosk.headquarter.dto.product;

import java.math.BigDecimal;
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

    private BigDecimal discountRate;

    private Boolean isDisplay;

    private String imageUrl;

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