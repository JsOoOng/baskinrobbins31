package com.kiosk.headquarter.dto.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadStoreProductUpdateRequestDTO {

    /*
     * 품절 여부
     */
    private Boolean isSoldOut;
}