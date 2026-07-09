package com.kiosk.customer.product.service;

import java.util.List;

import com.kiosk.customer.category.dto.CategoryResponse;
import com.kiosk.customer.product.dto.OrderCreateRequest;
import com.kiosk.customer.category.dto.CategoryResponse;
import com.kiosk.customer.product.dto.ProductCreateRequest;
import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.dto.ProductListResponse;

public interface ProductService {
    // 카테고리 전체 목록 조회
    List<CategoryResponse> getAllCategories();
    
    // 특정 지점의 특정 카테고리 상품 목록 조회 (STORE_PRODUCTS 테이블 조인하여 품절 필터링/표시)
    List<ProductListResponse> getProductsByCategory(Long storeId, Long categoryId);
    
    // 상품 상세 정보 조회 (PRODUCT_OPTIONS 및 STORE_FLAVORS를 조인하여 선택 가능한 옵션/맛 구성)
    ProductDetailResponse getProductDetail(Long storeId, Long productId);

	void addProduct(ProductCreateRequest request);

	void createOrder(OrderCreateRequest request);
}