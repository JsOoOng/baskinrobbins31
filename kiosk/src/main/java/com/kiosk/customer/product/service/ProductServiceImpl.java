package com.kiosk.customer.product.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.customer.category.dto.CategoryResponse;
import com.kiosk.customer.order.dto.OrderCreateRequest;
import com.kiosk.customer.product.dto.ProductCreateRequest;
import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.dto.ProductListResponse;
import com.kiosk.customer.product.repository.ProductOptionRepository;
import com.kiosk.customer.product.repository.ProductRepository; // 레포지토리 import
import com.kiosk.entity.Category;
import com.kiosk.entity.Product;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // 💡 의존성 자동 주입을 위해 필수
public class ProductServiceImpl implements ProductService {

    // 💡 DB에서 상품 정보를 조회하기 위해 레포지토리를 선언합니다.
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final EntityManager em;
    
    @Override
    public List<ProductListResponse> getProductsByCategory(Long storeId, Long categoryId) {
        // 🌟 DB에서 지점별 상품 및 품절 여부 조회
        List<Object[]> results = productRepository.findProductsWithSoldOutStatus(storeId, categoryId);
        List<ProductListResponse> responseList = new ArrayList<>();

        for (Object[] row : results) {
            Product product = (Product) row[0];
            Boolean isSoldOut = (Boolean) row[1];

            // DB 데이터를 프론트엔드가 요구하는 DTO 형식으로 변환
            ProductListResponse dto = new ProductListResponse();
            dto.setProductId(product.getId()); // 엔티티의 ID 필드명 확인 필요 (id 혹은 productId)
            dto.setCategoryId(categoryId.intValue()); // 🌟 누락된 카테고리 ID 설정 추가
            dto.setProductName(product.getProductName());
            dto.setBasePrice(product.getBasePrice());
            dto.setIsSoldOut(isSoldOut);
            dto.setImageUrl(product.getImageUrl());
            
            responseList.add(dto);
        }
        return responseList;
    }
    
    @Override
    public List<CategoryResponse> getAllCategories() {
        // 🌟 DB에서 전체 카테고리 조회해서 반환
        List<Category> categories = em.createQuery("SELECT c FROM Category c ORDER BY c.displayOrder", Category.class)
                                      .getResultList();
        
        List<CategoryResponse> responseList = new ArrayList<>();
        for (Category c : categories) {
            CategoryResponse dto = new CategoryResponse();
            dto.setCategoryId(c.getId());
            dto.setCategoryName(c.getCategoryName());
            responseList.add(dto);
        }
        return responseList;
    }

    @Override
    public ProductDetailResponse getProductDetail(Long storeId, Long productId) {
        Product product = productRepository.findById(productId.intValue())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            
        List<com.kiosk.entity.ProductOption> options = productOptionRepository.findByProductId(productId.intValue());
        List<com.kiosk.customer.product.dto.ProductOptionDto> optionDtos = new ArrayList<>();
        
        for (com.kiosk.entity.ProductOption opt : options) {
            com.kiosk.customer.product.dto.ProductOptionDto dto = new com.kiosk.customer.product.dto.ProductOptionDto();
            dto.setOptionId(opt.getId());
            dto.setProductId(product.getId());
            dto.setOptionType(opt.getOptionType().name());
            dto.setOptionName(opt.getOptionName());
            dto.setExtraPrice(opt.getExtraPrice());
            dto.setMaxFlavorCount(opt.getMaxFlavorCount());
            optionDtos.add(dto);
        }
        
        return new ProductDetailResponse(
            product.getId(),
            product.getProductName(),
            product.getDescription(),
            product.getBasePrice(),
            new ArrayList<>(), // optionGroups
            new ArrayList<>(), // availableFlavors
            optionDtos         // options
        );
    }

    @Override
    @Transactional
    public void addProduct(ProductCreateRequest request) {
        // TODO: 1. PRODUCTS 저장 -> 2. PRODUCT_OPTIONS 저장 -> 3. 모든 매장에 STORE_PRODUCTS 매핑 저장
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
    	for (OrderCreateRequest.ItemDto itemDto : request.getItems()) {
            int selectedFlavorCount = itemDto.getFlavors() != null ? itemDto.getFlavors().size() : 0;
            
            // 🚀 JPA 레포지토리를 통해 DB에서 동적으로 최대 맛 개수 제약 조건 가져오기
            Integer maxFlavors = productOptionRepository.findMaxFlavorCountByProductId(itemDto.getProductId());

            if (maxFlavors != null && maxFlavors > 0) {
                if (selectedFlavorCount > maxFlavors) {
                    throw new IllegalArgumentException("최대 선택 가능 맛 개수를 초과했습니다.");
                }
            }
            // 이후 주문 생성 MyBatis 처리 혹은 JPA 처리...
        }
        }
    }
