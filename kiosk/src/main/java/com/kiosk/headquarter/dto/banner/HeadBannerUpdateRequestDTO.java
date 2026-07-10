package com.kiosk.headquarter.dto.banner;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HeadBannerUpdateRequestDTO {

    private String title;
    private String imageUrl;
    private Boolean isActive;
}