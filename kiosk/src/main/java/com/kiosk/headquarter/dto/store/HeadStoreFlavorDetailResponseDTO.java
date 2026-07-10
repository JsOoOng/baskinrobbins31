package com.kiosk.headquarter.dto.store;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadStoreFlavorDetailResponseDTO {

    private Integer storeFlavorId;
    private Integer storeId;
    private String storeName;
    private Integer flavorId;
    private String flavorName;
    private Boolean flavorIsActive;
    private Integer container;
    private Boolean isSoldOut;
}