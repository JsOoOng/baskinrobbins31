package com.kiosk.headquarter.dto.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadStoreFlavorAddRequestDTO {

    private Integer flavorId;
    private Integer container;
    private Boolean isSoldOut;
}