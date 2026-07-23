package com.kiosk.headquarter.dto.product;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] HeadProductCreateRequestDTO
 *
 * <p>역할: 상품·메뉴 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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