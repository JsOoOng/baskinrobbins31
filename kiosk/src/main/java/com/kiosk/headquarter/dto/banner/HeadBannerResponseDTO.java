package com.kiosk.headquarter.dto.banner;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadBannerResponseDTO {

    private Integer bannerId;
    private String title;
    private String imageUrl;
    private Boolean isActive;
}