package com.kiosk.customer.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kiosk.customer.basket.dto.BasketAddRequest.FlavorDto;
import com.kiosk.customer.category.dto.CategoryDto;
import com.kiosk.customer.product.dto.ProductDto;
import com.kiosk.customer.product.dto.ProductOptionDto;

import java.util.List;

@Mapper
public interface ProductMapper {

    // 1. 카테고리 탭 목록 조회 (메인 화면)
    List<CategoryDto> getCategories();

    // 2. 카테고리별 상품 목록 조회
    List<ProductDto> getProductsByCategory(@Param("categoryId") int categoryId);

    // 3. 특정 상품의 상세 정보 조회
    ProductDto getProductById(@Param("productId") int productId);

    // 4. [핵심] 특정 상품에 매핑된 모든 선택 옵션 조회 (PRODUCT_OPTIONS 참조)
    List<ProductOptionDto> getProductOptionsByProductId(@Param("productId") int productId);

    // 5. [핵심] 현재 판매 중인 전체 아이스크림 맛 리스트 조회 (ICECREAM_FLAVORS 참조)
    List<FlavorDto> getActiveFlavors();
    
}