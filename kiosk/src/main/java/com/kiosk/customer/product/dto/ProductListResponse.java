package com.kiosk.customer.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    
    //현재는 기존 코드 호환을 위해 유지합니다.
    //고객 화면에서는 regularPrice와 finalPrice를 사용합니다
    private Integer basePrice;     // 상품 기본 가격
    
    private Integer discountRate;   // 상품 자체 할인율 (%)
    private Boolean isSoldOut;     // 지점 내 해당 상품 품절 여부 (STORE_PRODUCTS.is_sold_out)
    private String imageUrl;	   // 상품 이미지 파일 경로/URL (PRODUCTS.image_url)
    private Integer regularPrice;
    private Integer finalPrice;
}