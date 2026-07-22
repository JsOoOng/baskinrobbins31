package com.kiosk.headquarter.dto.flavor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadFlavorCreateRequestDTO {

    /*
     * 아이스크림 맛 이름
     *
     * 예:
     * 블랙 소르베
     */
    private String flavorName;

    /*
     * 아이스크림 맛 이미지 경로
     *
     * 반드시 다음 형식으로 입력합니다.
     *
     * /images/flavors/black_sorbet.png
     */
    private String imageUrl;
}