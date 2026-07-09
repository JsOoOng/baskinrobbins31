package com.kiosk.customer.product.service;

<<<<<<< Updated upstream
import com.kiosk.customer.flavor.category.dto.CategoryResponse;
=======
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.customer.category.dto.CategoryResponse;
import com.kiosk.customer.product.dto.OrderCreateRequest;
>>>>>>> Stashed changes
import com.kiosk.customer.product.dto.ProductCreateRequest;
import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.dto.ProductListResponse;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

   
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
	
	@Override
    @Transactional
    public void createOrder(OrderCreateRequest request) {
        
        // 상세 주문 항목들을 하나씩 순회하며 검증 및 적재
        for (OrderCreateRequest.ItemDto itemDto : request.getItems()) {
            
            // [기획안 2-1-2 반영] 아이스크림 맛 개수 제한 검증 로직 추가
            // 실제 프로젝트에서는 productId에 매핑된 상품명이나 상품 타입 정보가 필요합니다.
            // 여기서는 이해하기 쉽도록 하드코딩 예시로 처리합니다.
            
            int selectedFlavorCount = itemDto.getFlavors() != null ? itemDto.getFlavors().size() : 0;
            
            if (itemDto.getProductId() == 15) { // 예: 15번 상품이 '파인트'인 경우
                if (selectedFlavorCount > 3) {
                    throw new IllegalArgumentException("파인트 제품은 최대 3가지 맛만 선택할 수 있습니다.");
                }
            } else if (itemDto.getProductId() == 16) { // 예: 16번 상품이 '쿼터'인 경우
                if (selectedFlavorCount > 4) {
                    throw new IllegalArgumentException("쿼터 제품은 최대 4가지 맛만 선택할 수 있습니다.");
                }
            } else if (itemDto.getProductId() == 17) { // 예: 17번 상품이 '패밀리'인 경우
                if (selectedFlavorCount > 5) {
                    throw new IllegalArgumentException("패밀리 제품은 최대 5가지 맛만 선택할 수 있습니다.");
                }
            } else if (itemDto.getProductId() == 18) { // 예: 18번 상품이 '하프겔런'인 경우
                if (selectedFlavorCount > 6) {
                    throw new IllegalArgumentException("하프겔런 제품은 최대 6가지 맛만 선택할 수 있습니다.");
                }
            }
        }

            // ------------------------------------------------------------------
            // 검증을 통과하면 아래의 실제 DB Insert 로직이 안전하게 실행됩니다.
            // ------------------------------------------------------------------
            // 1. ORDER_ITEMS 테이블 적재...
            // 2. ORDER_ITEM_FLAVORS 테이블 적재...
        }
	
}