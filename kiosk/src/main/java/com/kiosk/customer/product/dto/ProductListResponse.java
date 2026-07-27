package com.kiosk.customer.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [코드 흐름 안내] ProductListResponse
 *
 * <p>역할: 상품·메뉴 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {
    
    private Integer productId;      // 상품 고유 식별자 (PRODUCTS.product_id)
    private Integer categoryId;     // 소속 카테고리 식별자 (PRODUCTS.category_id)
    private String productName;    // 상품명 (예: 파인트, 싱글레귤러)
    private String description;    // 상품 상세 설명
    private Integer basePrice;     // 상품 기본 가격
    private Integer finalPrice;    // 상품 최종 가격 (할인 적용)
    private Double discountRate;   // 상품 전체 할인율(%)
    private Boolean isSoldOut;     // 지점 내 해당 상품 품절 여부 (STORE_PRODUCTS.is_sold_out)
    private String imageUrl;	   // 상품 이미지 파일 경로/URL (PRODUCTS.image_url)
}