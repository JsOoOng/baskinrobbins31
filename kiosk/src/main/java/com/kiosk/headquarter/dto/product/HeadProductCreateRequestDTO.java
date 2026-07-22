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

    private Integer categoryId;

    private String productName;

    private String description;

    private Integer basePrice;

    private Integer discountRate;

    private Boolean isDisplay;

    private String imageUrl;

    @Builder.Default
    private List<Integer> storeIds =
            new ArrayList<>();

    @Builder.Default
    private List<String> flavorNames =
            new ArrayList<>();

    private String inventoryItemName;
    private String inventoryUnit;
    private Integer inventoryUnitPrice;
}