package com.kiosk.headquarter.dto.store;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] HeadStoreProductListResponseDTO
 *
 * <p>역할: 지점 판매 상품 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
public class HeadStoreProductListResponseDTO {

    private Integer storeProductId;

    private Integer storeId;

    private Integer productId;

    private String productName;

    /*
     * 본사 기본 가격
     */
    private Integer basePrice;

    /*
     * 해당 지점 판매 가격
     */
    private Integer storeProductPrice;

    private Boolean isSoldOut;
}