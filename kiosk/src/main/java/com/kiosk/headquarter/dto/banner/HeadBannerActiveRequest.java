package com.kiosk.headquarter.dto.banner;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadBannerActiveRequest {

    @NotNull(
            message = "배너 노출 여부가 필요합니다."
    )
    private Boolean isActive;
}