package com.kiosk.headquarter.service;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Category;
import com.kiosk.entity.IcecreamFlavor;
import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Product;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.StoreProduct;
import com.kiosk.entity.enums.AutoRestockMode;
import com.kiosk.headquarter.dto.product.HeadProductCreateRequestDTO;
import com.kiosk.headquarter.dto.product.HeadProductResponseDTO;
import com.kiosk.headquarter.repository.HeadCategoryMapper;
import com.kiosk.headquarter.repository.HeadFlavorMapper;
import com.kiosk.headquarter.repository.HeadInventoryItemMapper;
import com.kiosk.headquarter.repository.HeadProductMapper;
import com.kiosk.headquarter.repository.HeadStoreInventoryMapper;
import com.kiosk.headquarter.repository.HeadStoreMapper;
import com.kiosk.headquarter.repository.HeadStoreProductMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadProductService {

    private static final String
            ICECREAM_CATEGORY_NAME =
            "아이스크림";
    
    private static final Set<String>
    FLAVOR_CATEGORY_NAMES =
    Set.of(
            "아이스크림",
            "아이스크림 케이크"
    );

    private final HeadProductMapper
            headProductMapper;

    private final HeadCategoryMapper
            headCategoryMapper;
    
    private final HeadFlavorMapper
            headFlavorMapper;

    private final HeadInventoryItemMapper
            headInventoryItemMapper;

    /*
     * 지점 판매 상품 및 지점 재고 등록에 사용합니다.
     */
    private final HeadStoreMapper 
    		headStoreMapper;
    
    private final HeadStoreProductMapper 
    		headStoreProductMapper;
    
    private final HeadStoreInventoryMapper 
    		headStoreInventoryMapper;
    
    /*
     * 본사 상품 등록
     *
     * 처리 순서:
     * 1. PRODUCTS 저장
     * 2. INVENTORY_ITEMS 저장
     * 3. STORE_PRODUCTS 저장
     * 4. STORE_INVENTORY 저장(current_stock = 0)
     * 
     * 아이스크림 카테고리 여부 확인
     *
     * 현재는 외래키나 categoryCode가 없으므로
     * 카테고리 이름을 기준으로 판단합니다.
     */
    private boolean isIcecreamCategory(
            Category category
    ) {

        if (
                category == null ||
                category.getCategoryName() == null
        ) {
            return false;
        }

        return ICECREAM_CATEGORY_NAME.equals(
                category
                        .getCategoryName()
                        .trim()
        );
    }
    @Transactional
    public HeadProductResponseDTO createProduct(
            HeadProductCreateRequestDTO requestDTO
    ) {

        validateCreateRequest(requestDTO);

        /*
         * 1. 카테고리 조회
         */
        Category category = headCategoryMapper
                .findById(
                        requestDTO.getCategoryId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 카테고리입니다."
                        )
                );

        String productName =
                requestDTO.getProductName().trim();

        boolean icecreamCategory =
                isIcecreamCategory(category);
        
        Set<String> flavorNames =
                normalizeFlavorNames(
                        requestDTO.getFlavorNames()
                );

        if (
                supportsFlavor(category) &&
                flavorNames.isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "아이스크림 상품은 맛을 한 개 이상 선택해주세요."
            );
        }

        /*
         * 아이스크림 맛 이름 중복 검사
         */
        if (
                icecreamCategory &&
                headFlavorMapper
                        .existsByFlavorName(
                                productName
                        )
        ) {
            throw new IllegalArgumentException(
                    "이미 등록된 아이스크림 맛입니다."
            );
        }

        /*
         * 2. PRODUCTS 저장
         */
        Product product = Product.builder()
        		.category(category)
        		.productName(productName)
        		.description(
        		    requestDTO.getDescription()
        		)
        		.basePrice(
        		    requestDTO.getBasePrice()
        		)
        		.discountRate(
        		    requestDTO.getDiscountRate() != null
        		        ? requestDTO.getDiscountRate()
        		        : 0
        		)
        		.isDisplay(
        		    requestDTO.getIsDisplay() != null
        		        ? requestDTO.getIsDisplay()
        		        : true
        		)
        		.imageUrl(
        		    requestDTO.getImageUrl()
        		)
        		.build();

        Product savedProduct =
                headProductMapper
                        .saveAndFlush(product);
        
        /*
         * 아이스크림 또는 아이스크림 케이크이면
         * 선택된 맛 중 기존에 없는 맛만 저장합니다.
         */
        if (supportsFlavor(category)) {
            saveMissingFlavors(flavorNames);
        }
        
        System.out.println(
                "상품 저장 완료: productId="
                        + savedProduct.getId()
        );
     
        
        /*
         * 3. 아이스크림 맛 저장
         */
        if (icecreamCategory) {

            IcecreamFlavor flavor =
                    IcecreamFlavor.builder()
                            .flavorName(
                                    savedProduct
                                            .getProductName()
                            )
                            .isActive(
                                    Boolean.TRUE.equals(
                                            savedProduct
                                                    .getIsDisplay()
                                    )
                            )
                            .imageUrl(
                                    savedProduct
                                            .getImageUrl()
                            )
                            .build();

            IcecreamFlavor savedFlavor =
                    headFlavorMapper
                            .saveAndFlush(flavor);

            System.out.println(
                    "아이스크림 맛 저장 완료: "
                            + "flavorId="
                            + savedFlavor.getId()
                            + ", flavorName="
                            + savedFlavor
                                    .getFlavorName()
            );
        }

        /*
         * 4. INVENTORY_ITEMS 저장
         */
        InventoryItem inventoryItem =
                InventoryItem.builder()
                        .product(savedProduct)
                        .unit("EA")
                        .unitPrice(
                                savedProduct.getBasePrice()
                        )
                        .build();

        InventoryItem savedItem =
                headInventoryItemMapper
                        .saveAndFlush(
                                inventoryItem
                        );

        System.out.println(
                "재고 품목 저장 완료: itemId="
                        + savedItem.getId()
        );

        /*
         * 중복 지점 ID 제거
         *
         * 예:
         * [1, 1, 2]
         * →
         * [1, 2]
         */
        Set<Integer> storeIds =
                new LinkedHashSet<>(
                        requestDTO.getStoreIds()
                );

        /*
         * 4. 선택한 지점을 반복 처리
         */
        for (Integer storeId : storeIds) {

            if (storeId == null || storeId <= 0) {
                throw new IllegalArgumentException(
                        "잘못된 지점 ID입니다: "
                                + storeId
                );
            }

            /*
             * 4-1. 지점 조회
             */
            Store store = headStoreMapper
                    .findById(storeId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "존재하지 않는 지점입니다. storeId="
                                            + storeId
                            )
                    );

            /*
             * 4-2. STORE_PRODUCTS 저장
             *
             * 해당 지점에서 새 상품을 판매할 수 있도록 연결합니다.
             * 해당 지점에 삭제되지 않은 동일 상품 연결이 있는지 확인합니다.
            */
            boolean storeProductExists =
                    headStoreProductMapper
                            .existsByStore_IdAndProduct_IdAndIsDeletedFalse(
                                    store.getId(),
                                    savedProduct.getId()
                            );

            if (!storeProductExists) {

                StoreProduct storeProduct =
                        StoreProduct.builder()
                                .store(store)
                                .product(savedProduct)

                                /*
                                 * STORE_PRODUCTS의
                                 * store_product_price는 NOT NULL이므로
                                 * 반드시 값을 넣어야 합니다.
                                // 현재는 본사 상품 기본 가격을
                				// 지점 판매 가격으로 사용
                                */
                                .isSoldOut(false)
                                .isDeleted(false)
                                .build();

                StoreProduct savedStoreProduct =
                        headStoreProductMapper
                                .saveAndFlush(storeProduct);

                System.out.println(
                        "지점 판매 상품 저장 완료: "
                                + "storeProductId="
                                + savedStoreProduct.getId()
                                + ", storeId="
                                + store.getId()
                                + ", productId="
                                + savedProduct.getId()
                                + ", storeProductPrice="
                                + savedProduct.getBasePrice()
                );
            }

            /*
             * 4-3. STORE_INVENTORY 저장
             *
             * 새로 생성된 InventoryItem을 해당 지점에 연결하고,
             * 최초 재고를 0으로 저장합니다.
             */
            boolean storeInventoryExists =
                    headStoreInventoryMapper
                            .existsByStoreAndItem(
                                    store,
                                    savedItem
                            );

            if (!storeInventoryExists) {

            	StoreInventory storeInventory =
            	        StoreInventory.builder()
            	                .store(store)
            	                .item(savedItem)
            	                .currentStock(0)
            	                .minStock(10)
            	                .targetStock(50)
            	                .autoRestockEnabled(true)
            	                .restockMode(
            	                        AutoRestockMode.THRESHOLD
            	                )
            	                .build();

                StoreInventory savedStoreInventory =
                        headStoreInventoryMapper
                                .saveAndFlush(storeInventory);

                System.out.println(
                        "지점 재고 저장 완료: "
                                + "storeInventoryId="
                                + savedStoreInventory.getId()
                                + ", storeId="
                                + store.getId()
                                + ", itemId="
                                + savedItem.getId()
                                + ", currentStock="
                                + savedStoreInventory.getCurrentStock()
                );
            }
        }

        return toResponseDTO(savedProduct);
    }

    /*
     * 본사 상품 목록 조회
     */
    public List<HeadProductResponseDTO> getProductList() {

        return headProductMapper
                .findByIsDisplayTrueOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /*
     * 본사 상품 상세 조회
     */
    public HeadProductResponseDTO getProductDetail(
            Integer productId
    ) {

        Product product = headProductMapper
                .findByIdAndIsDisplayTrue(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 상품입니다."
                        )
                );

        return toResponseDTO(product);
    }

    /*
     * 본사 상품 수정
     */
    @Transactional
    public HeadProductResponseDTO updateProduct(
            Integer productId,
            HeadProductCreateRequestDTO requestDTO
    ) {

        Product product = headProductMapper
                .findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 상품입니다."
                        )
                );

        Category category = headCategoryMapper
                .findById(requestDTO.getCategoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 카테고리입니다."
                        )
                );

        product.updateProduct(
                category,
                requestDTO.getProductName(),
                requestDTO.getDescription(),
                requestDTO.getBasePrice(),

                requestDTO.getDiscountRate() != null
                        ? requestDTO.getDiscountRate()
                        : 0,

                product.getMarginRate(),

                requestDTO.getIsDisplay() != null
                        ? requestDTO.getIsDisplay()
                        : true,

                product.getImageUrl()
        );

        return toResponseDTO(product);
    }

    /*
     * 본사 상품 삭제 처리
     */
    @Transactional
    public void deleteProduct(Integer productId) {

        Product product = headProductMapper
                .findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 상품입니다."
                        )
                );

        product.hideProduct();
    }

    /*
     * 상품 등록 요청값 검증
     */
    private void validateCreateRequest(
            HeadProductCreateRequestDTO requestDTO
    ) {

        if (requestDTO == null) {
            throw new IllegalArgumentException(
                    "상품 등록 요청이 없습니다."
            );
        }

        if (requestDTO.getCategoryId() == null) {
            throw new IllegalArgumentException(
                    "카테고리를 선택해주세요."
            );
        }

        if (
                requestDTO.getProductName() == null ||
                requestDTO.getProductName().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "상품명을 입력해주세요."
            );
        }

        if (
                requestDTO.getBasePrice() == null ||
                requestDTO.getBasePrice() < 0
        ) {
            throw new IllegalArgumentException(
                    "기본 가격은 0 이상이어야 합니다."
            );
        }

        if (
                requestDTO.getStoreIds() == null ||
                requestDTO.getStoreIds().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "판매 지점을 한 개 이상 선택해주세요."
            );
        }
    }

    /*
     * 응답 DTO 변환
     */
    private HeadProductResponseDTO toResponseDTO(
            Product product
    ) {

        return HeadProductResponseDTO.builder()
                .productId(product.getId())
                .categoryId(product.getCategory().getId())
                .categoryName(
                        product.getCategory()
                                .getCategoryName()
                )
                .productName(product.getProductName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .discountRate(product.getDiscountRate())
                .isDisplay(product.getIsDisplay())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .build();
    }
    
    /*
     * 맛을 사용하는 카테고리 여부
     */
    private boolean supportsFlavor(
            Category category
    ) {

        if (
                category == null ||
                category.getCategoryName() == null
        ) {
            return false;
        }

        return FLAVOR_CATEGORY_NAMES.contains(
                category
                        .getCategoryName()
                        .trim()
        );
    }

    /*
     * 맛 이름 정리
     *
     * null, 빈 문자열, 중복값을 제거합니다.
     */
    private Set<String> normalizeFlavorNames(
            List<String> flavorNames
    ) {

        Set<String> normalizedNames =
                new LinkedHashSet<>();

        if (flavorNames == null) {
            return normalizedNames;
        }

        for (String flavorName : flavorNames) {

            if (
                    flavorName == null ||
                    flavorName.isBlank()
            ) {
                continue;
            }

            normalizedNames.add(
                    flavorName.trim()
            );
        }

        return normalizedNames;
    }

    /*
     * ICECREAM_FLAVORS에 없는 맛만 신규 등록
     */
    private void saveMissingFlavors(
            Set<String> flavorNames
    ) {

        for (String flavorName : flavorNames) {

            boolean flavorExists =
                    headFlavorMapper
                            .existsByFlavorName(
                                    flavorName
                            );

            if (flavorExists) {
                continue;
            }

            IcecreamFlavor flavor =
                    IcecreamFlavor.builder()
                            .flavorName(flavorName)
                            .isActive(true)

                            /*
                             * 상품 이미지는 파인트·케이크 등
                             * 상품 이미지일 수 있으므로
                             * 맛 이미지에는 복사하지 않습니다.
                             */
                            .imageUrl(null)
                            .build();

            headFlavorMapper.save(flavor);
        }
    }
}