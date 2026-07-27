package com.kiosk.customer.product.service;

import java.util.List;

import com.kiosk.customer.category.dto.CategoryResponse;
import com.kiosk.customer.order.dto.OrderCreateRequest;
import com.kiosk.customer.product.dto.ProductCreateRequest;
import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.dto.ProductListResponse;

/**
 * [코드 흐름 안내] ProductService
 *
 * <p>역할: 고객 키오스크의 상품·메뉴 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface ProductService {
    // 카테고리 전체 목록 조회
    List<com.kiosk.customer.category.dto.CategoryResponse> getAllCategories();
    
    // 특정 지점의 특정 카테고리 상품 목록 조회 (STORE_PRODUCTS 테이블 조인하여 품절 필터링/표시)
    List<ProductListResponse> getProductsByCategory(Long storeId, Long categoryId);
    
    // 상품 상세 정보 조회 (PRODUCT_OPTIONS 및 STORE_FLAVORS를 조인하여 선택 가능한 옵션/맛 구성)
    ProductDetailResponse getProductDetail(Long storeId, Long productId);

	void addProduct(ProductCreateRequest request);

	void createOrder(OrderCreateRequest request);
}