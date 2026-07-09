package com.kiosk.customer.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.customer.category.dto.CategoryResponse;
import com.kiosk.customer.product.dto.ProductCreateRequest;
import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.dto.ProductListResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    // 추후 레포지토리들 주입 예정

    @Override
    public List<ProductListResponse> getProductsByCategory(Long storeId, Long categoryId) {
        // TODO: STORE_PRODUCTS와 PRODUCTS 조인하여 해당 카테고리 상품 리스트 조회
        return null;
    }

    @Override
    public ProductDetailResponse getProductDetail(Long storeId, Long productId) {
        // TODO: PRODUCT_OPTIONS 조회하여 OptionGroupDto로 묶고, FlavorService를 통해 맛 목록을 가져와 병합
        return null;
    }

    @Override
    @Transactional
    public void addProduct(ProductCreateRequest request) {
        // TODO: 1. PRODUCTS 저장 -> 2. PRODUCT_OPTIONS 저장 -> 3. 모든 매장에 STORE_PRODUCTS 매핑 저장
    }

	@Override
	public List<CategoryResponse> getAllCategories() {
		// TODO Auto-generated method stub
		return null;
	}
}