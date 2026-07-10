package com.kiosk.headquarter.dto.product;

import com.kiosk.entity.enums.OptionType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadProductOptionResponseDTO {

    private Integer optionId;
    private Integer productId;
    private String productName;

    private OptionType optionType;
    private String optionName;
    private Integer extraPrice;
    private Integer maxFlavorCount;
}