package com.kiosk.headquarter.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Category;
import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Product;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.StoreProduct;
import com.kiosk.entity.enums.AutoRestockMode;
import com.kiosk.headquarter.dto.product.HeadProductCreateRequestDTO;
import com.kiosk.headquarter.dto.product.HeadProductResponseDTO;
import com.kiosk.headquarter.repository.HeadCategoryMapper;
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

    private final HeadProductMapper
            headProductMapper;

    private final HeadCategoryMapper
            headCategoryMapper;

    private final HeadInventoryItemMapper
            headInventoryItemMapper;

    /*
     * 지점 판매 상품 및
     * 지점 재고 등록에 사용합니다.
     */
    private final HeadStoreMapper
            headStoreMapper;

    private final HeadStoreProductMapper
            headStoreProductMapper;

    private final HeadStoreInventoryMapper
            headStoreInventoryMapper;

    private final AdminLogService
            adminLogService;

    /*
     * 본사 상품 등록
     *
     * 처리 순서:
     *
     * 1. PRODUCTS 저장
     * 2. INVENTORY_ITEMS 저장
     * 3. STORE_PRODUCTS 저장
     * 4. STORE_INVENTORY 저장
     *
     * 상품 등록에서는
     * ICECREAM_FLAVORS를 처리하지 않습니다.
     *
     * 아이스크림 맛은
     * HeadFlavorService를 통해
     * 별도로 등록합니다.
     */
    @Transactional
    public HeadProductResponseDTO createProduct(
            HeadProductCreateRequestDTO requestDTO
    ) {

        validateCreateRequest(
                requestDTO
        );

        /*
         * 1. 카테고리 조회
         */
        Category category =
                headCategoryMapper
                        .findById(
                                requestDTO
                                        .getCategoryId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 카테고리입니다."
                                )
                        );

        String productName =
                requestDTO
                        .getProductName()
                        .trim();

        /*
         * 2. PRODUCTS 저장
         *
         * 카테고리가 아이스크림이어도
         * 상품 정보만 저장합니다.
         */
        Product product =
                Product.builder()
                        .category(
                                category
                        )
                        .productName(
                                productName
                        )
                        .description(
                                normalizeNullable(
                                        requestDTO
                                                .getDescription()
                                )
                        )
                        .basePrice(
                                requestDTO
                                        .getBasePrice()
                        )
                        .discountRate(
                                requestDTO
                                        .getDiscountRate() != null
                                        ? requestDTO
                                                .getDiscountRate()
                                        : 0
                        )
                        .isDisplay(
                                requestDTO
                                        .getIsDisplay() != null
                                        ? requestDTO
                                                .getIsDisplay()
                                        : true
                        )
                        .imageUrl(
                                normalizeNullable(
                                        requestDTO
                                                .getImageUrl()
                                )
                        )
                        .build();

        Product savedProduct =
                headProductMapper
                        .saveAndFlush(
                                product
                        );

        System.out.println(
                "상품 저장 완료: productId="
                        + savedProduct.getId()
        );

        /*
         * 3. INVENTORY_ITEMS 저장
         */
        InventoryItem inventoryItem =
                InventoryItem.builder()
                        .product(
                                savedProduct
                        )
                        .itemName(
                                savedProduct
                                        .getProductName()
                        )
                        .unit("개")
                        .unitPrice(
                                savedProduct
                                        .getBasePrice()
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
         * [1, 1, 2]
         * →
         * [1, 2]
         */
        Set<Integer> storeIds =
                new LinkedHashSet<>(
                        requestDTO
                                .getStoreIds()
                );

        /*
         * 선택한 지점을 반복 처리
         */
        for (
                Integer storeId
                : storeIds
        ) {

            if (
                    storeId == null ||
                    storeId <= 0
            ) {
                throw new IllegalArgumentException(
                        "잘못된 지점 ID입니다: "
                                + storeId
                );
            }

            /*
             * 4. 지점 조회
             */
            Store store =
                    headStoreMapper
                            .findById(
                                    storeId
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "존재하지 않는 지점입니다. storeId="
                                                    + storeId
                                    )
                            );

            /*
             * 5. STORE_PRODUCTS 저장
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
                                .store(
                                        store
                                )
                                .product(
                                        savedProduct
                                )
                                .storeProductPrice(
                                        savedProduct
                                                .getBasePrice()
                                )
                                .isSoldOut(false)
                                .isDeleted(false)
                                .build();

                StoreProduct savedStoreProduct =
                        headStoreProductMapper
                                .saveAndFlush(
                                        storeProduct
                                );

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
             * 6. STORE_INVENTORY 저장
             *
             * 새 상품의 최초 재고는 0입니다.
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
                                .store(
                                        store
                                )
                                .item(
                                        savedItem
                                )
                                .currentStock(0)
                                .minStock(10)
                                .targetStock(50)
                                .autoRestockEnabled(true)
                                .restockMode(
                                        AutoRestockMode
                                                .THRESHOLD
                                )
                                .build();

                StoreInventory savedStoreInventory =
                        headStoreInventoryMapper
                                .saveAndFlush(
                                        storeInventory
                                );

                System.out.println(
                        "지점 재고 저장 완료: "
                                + "storeInventoryId="
                                + savedStoreInventory.getId()
                                + ", storeId="
                                + store.getId()
                                + ", itemId="
                                + savedItem.getId()
                                + ", currentStock="
                                + savedStoreInventory
                                        .getCurrentStock()
                );
            }
        }

        adminLogService.logAction(
                "상품",
                productName
                        + " 신규 등록"
        );

        return toResponseDTO(
                savedProduct
        );
    }

    /*
     * 본사 상품 목록 조회
     */
    public List<HeadProductResponseDTO>
            getProductList() {

        return headProductMapper
                .findByIsDisplayTrueOrderByIdDesc()
                .stream()
                .map(
                        this::toResponseDTO
                )
                .toList();
    }

    /*
     * 본사 상품 상세 조회
     */
    public HeadProductResponseDTO
            getProductDetail(
                    Integer productId
            ) {

        Product product =
                headProductMapper
                        .findByIdAndIsDisplayTrue(
                                productId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 상품입니다."
                                )
                        );

        return toResponseDTO(
                product
        );
    }

    /*
     * 본사 상품 수정
     */
    @Transactional
    public HeadProductResponseDTO updateProduct(
            Integer productId,
            HeadProductCreateRequestDTO requestDTO
    ) {

        if (requestDTO == null) {
            throw new IllegalArgumentException(
                    "상품 수정 요청이 없습니다."
            );
        }

        if (
                requestDTO.getCategoryId() == null
        ) {
            throw new IllegalArgumentException(
                    "카테고리를 선택해주세요."
            );
        }

        if (
                requestDTO.getProductName() == null ||
                requestDTO
                        .getProductName()
                        .isBlank()
        ) {
            throw new IllegalArgumentException(
                    "상품명을 입력해주세요."
            );
        }

        Product product =
                headProductMapper
                        .findById(
                                productId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 상품입니다."
                                )
                        );

        Category category =
                headCategoryMapper
                        .findById(
                                requestDTO
                                        .getCategoryId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 카테고리입니다."
                                )
                        );

        product.updateProduct(
                category,

                requestDTO
                        .getProductName()
                        .trim(),

                normalizeNullable(
                        requestDTO
                                .getDescription()
                ),

                requestDTO
                        .getBasePrice(),

                requestDTO
                        .getDiscountRate() != null
                        ? requestDTO
                                .getDiscountRate()
                        : 0,

                product.getMarginRate(),

                requestDTO
                        .getIsDisplay() != null
                        ? requestDTO
                                .getIsDisplay()
                        : true,

                product.getImageUrl()
        );

        adminLogService.logAction(
                "상품",
                product.getProductName()
                        + " 정보 수정"
        );

        return toResponseDTO(
                product
        );
    }

    /*
     * 본사 상품 삭제 처리
     */
    @Transactional
    public void deleteProduct(
            Integer productId
    ) {

        Product product =
                headProductMapper
                        .findById(
                                productId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 상품입니다."
                                )
                        );

        product.hideProduct();

        adminLogService.logAction(
                "상품",
                product.getProductName()
                        + " 삭제"
        );
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

        if (
                requestDTO.getCategoryId() == null
        ) {
            throw new IllegalArgumentException(
                    "카테고리를 선택해주세요."
            );
        }

        if (
                requestDTO.getProductName() == null ||
                requestDTO
                        .getProductName()
                        .isBlank()
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
                requestDTO
                        .getStoreIds()
                        .isEmpty()
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

        return HeadProductResponseDTO
                .builder()
                .productId(
                        product.getId()
                )
                .categoryId(
                        product.getCategory()
                                .getId()
                )
                .categoryName(
                        product.getCategory()
                                .getCategoryName()
                )
                .productName(
                        product.getProductName()
                )
                .description(
                        product.getDescription()
                )
                .basePrice(
                        product.getBasePrice()
                )
                .discountRate(
                        product.getDiscountRate()
                )
                .isDisplay(
                        product.getIsDisplay()
                )
                .imageUrl(
                        product.getImageUrl()
                )
                .createdAt(
                        product.getCreatedAt()
                )
                .build();
    }

    /*
     * 선택 입력 문자열 정리
     */
    private String normalizeNullable(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}