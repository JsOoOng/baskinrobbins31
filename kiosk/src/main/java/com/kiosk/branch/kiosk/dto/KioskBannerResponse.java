package com.kiosk.branch.kiosk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KioskBannerResponse {
    private Integer kioskId;
    private Integer bannerId;
    private String bannerTitle;
    private String bannerImageUrl;
}
