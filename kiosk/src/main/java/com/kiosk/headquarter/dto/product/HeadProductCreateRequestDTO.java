package com.kiosk.headquarter.dto.product;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HeadProductCreateRequestDTO {

    /*
     * 상품 카테고리 ID
     */
    private Integer categoryId;

    /*
     * 상품명
     *
     * 예:
     * 싱글 레귤러
     * 파인트
     * 블랙 소르베 아이스크림
     */
    private String productName;

    /*
     * 상품 설명
     */
    private String description;

    /*
     * 상품 기본 가격
     */
    private Integer basePrice;

    /*
     * 할인율
     *
     * 0 ~ 100
     */
    private Integer discountRate;

    /*
     * 고객 화면 노출 여부
     */
    private Boolean isDisplay;

    /*
     * 상품 이미지 경로
     *
     * IcecreamFlavor의 imageUrl과는
     * 완전히 다른 값입니다.
     *
     * 예:
     * /images/products/single_regular.png
     */
    private String imageUrl;

    /*
     * 판매할 지점 ID 목록
     *
     * 상품 신규 등록 시 사용합니다.
     */
    private List<Integer> storeIds;
}