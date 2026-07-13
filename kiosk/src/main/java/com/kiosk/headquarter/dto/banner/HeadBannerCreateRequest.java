package com.kiosk.headquarter.dto.banner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadBannerCreateRequest {

    @Size(
            max = 100,
            message = "배너 제목은 100자 이하여야 합니다."
    )
    private String title;

    @NotBlank(
            message = "배너 이미지 URL은 필수입니다."
    )
    @Size(
            max = 255,
            message = "이미지 URL은 255자 이하여야 합니다."
    )
    private String imageUrl;

    private Boolean isActive;
}