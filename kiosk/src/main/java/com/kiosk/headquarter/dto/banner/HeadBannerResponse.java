package com.kiosk.headquarter.dto.banner;

import com.kiosk.entity.Banner;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadBannerResponse {

    private Integer bannerId;

    private String title;

    private String imageUrl;

    private Boolean isActive;

    public static HeadBannerResponse from(
            Banner banner
    ) {
        return HeadBannerResponse.builder()
                .bannerId(banner.getId())
                .title(banner.getTitle())
                .imageUrl(banner.getImageUrl())
                .isActive(banner.getIsActive())
                .build();
    }
}