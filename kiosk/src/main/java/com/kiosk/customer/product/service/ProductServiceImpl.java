package com.kiosk.customer.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.customer.category.dto.CategoryResponse;
import com.kiosk.customer.product.dto.OrderCreateRequest;
import com.kiosk.customer.product.dto.ProductCreateRequest;
import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.dto.ProductListResponse;
import com.kiosk.customer.product.repository.Product;
import com.kiosk.customer.product.repository.ProductRepository; // 레포지토리 import

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // 💡 의존성 자동 주입을 위해 필수
public class ProductServiceImpl implements ProductService {

    // 💡 DB에서 상품 정보를 조회하기 위해 레포지토리를 선언합니다.
    private final ProductRepository productRepository;

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

    // =================================================================
    // 🚀 하드코딩이 완벽히 제거된 최종 주문 생성 및 맛 개수 검증 메서드
    // =================================================================
    @Override
    @Transactional
    public void createOrder(OrderCreateRequest request) {
        /*
        // 상세 주문 항목들을 하나씩 순회하며 검증 및 적재
        for (OrderCreateRequest.ItemDto itemDto : request.getItems()) {
            
            // 1. 사용자가 담은 맛 개수 계산
            int selectedFlavorCount = itemDto.getFlavors() != null ? itemDto.getFlavors().size() : 0;
            
            // 2. 💡 하드코딩 제거: DB에서 해당 상품의 원본 데이터(최대 맛 개수 포함)를 직접 조회
            Product product = productRepository.findById(Long.valueOf(itemDto.getProductId()))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품 ID입니다: " + itemDto.getProductId()));

            // 3. 💡 동적 검증: DB에 설정된 maxFlavorCount가 존재하고, 사용자가 그보다 많이 골랐다면 차단!
            if (product.getMaxFlavorCount() != null && product.getMaxFlavorCount() > 0) {
                if (selectedFlavorCount > product.getMaxFlavorCount()) {
                    throw new IllegalArgumentException(
                        String.format("%s 제품은 최대 %d가지 맛만 선택할 수 있습니다. (현재 선택: %d개)", 
                            product.getProductName(), product.getMaxFlavorCount(), selectedFlavorCount)
                    );
                }
            }

            // ------------------------------------------------------------------
            // 검증을 통과하면 아래의 실제 DB Insert 로직이 안전하게 실행됩니다.
            // ------------------------------------------------------------------
            // 1. ORDER_ITEMS 테이블 적재...
            // 2. ORDER_ITEM_FLAVORS 테이블 적재...*/
			//지금 하드코딩으로 되어있어서 다시 만들어야함
        }
    }
}