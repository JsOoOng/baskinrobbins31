package com.kiosk.headquarter.dto.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadStoreFlavorUpdateRequestDTO {

    private Integer container;
    private Boolean isSoldOut;
}