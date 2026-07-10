package com.kiosk.headquarter.dto.flavor;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadFlavorResponseDTO {

    private Integer flavorId;
    private String flavorName;
    private Boolean isActive;
    private String imageUrl;
}