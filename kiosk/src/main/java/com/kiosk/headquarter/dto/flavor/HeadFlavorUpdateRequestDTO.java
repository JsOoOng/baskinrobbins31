package com.kiosk.headquarter.dto.flavor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadFlavorUpdateRequestDTO {

    private String flavorName;
    private Boolean isActive;
    private String imageUrl;
}