package com.kiosk.headquarter.dto.product;

import com.kiosk.entity.enums.OptionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadProductOptionCreateRequestDTO {

    private OptionType optionType;
    private String optionName;
    private Integer extraPrice;
    private Integer maxFlavorCount;
}