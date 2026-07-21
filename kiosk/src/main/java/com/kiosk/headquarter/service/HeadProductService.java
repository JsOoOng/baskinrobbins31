package com.kiosk.headquarter.service;

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

    private static final Set<String> FLAVOR_CATEGORY_NAMES =
            Set.of(
                    "아이스크림",
                    "아이스크림 케이크"
            );

    private static final int DEFAULT_DISCOUNT_RATE = 0;
    private static final int DEFAULT_MARGIN_RATE = 65;

    private final HeadProductMapper headProductMapper;
    private final HeadCategoryMapper headCategoryMapper;
    private final HeadFlavorMapper headFlavorMapper;
    private final HeadInventoryItemMapper headInventoryItemMapper;
    private final HeadStoreMapper headStoreMapper;
    private final HeadStoreProductMapper headStoreProductMapper;
    private final HeadStoreInventoryMapper headStoreInventoryMapper;

    @Transactional
    public HeadProductResponseDTO createProduct(
            HeadProductCreateRequestDTO requestDTO
    ) {

        validateCreateRequest(requestDTO);

        Category category = headCategoryMapper
                .findById(requestDTO.getCategoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 카테고리입니다."
                        )
                );

        String productName =
                requestDTO.getProductName().trim();

        Integer discountRate =
                resolveDiscountRate(
                        requestDTO.getDiscountRate()
                );

        Integer marginRate =
                resolveMarginRate(
                        requestDTO.getMarginRate()
                );

        validatePriceRates(
                discountRate,
                marginRate
        );

        Integer regularPrice =
                calculateRegularPrice(
                        requestDTO.getBasePrice(),
                        marginRate
                );

        Integer finalPrice =
                calculateFinalPrice(
                        regularPrice,
                        discountRate
                );

        validateNewFlavorRequest(
                category,
                requestDTO
        );

        Product product = Product.builder()
                .category(category)
                .productName(productName)
                .description(
                        normalizeText(
                                requestDTO.getDescription()
                        )
                )
                .basePrice(
                        requestDTO.getBasePrice()
                )
                .discountRate(
                        discountRate
                )
                .marginRate(
                        marginRate
                )
                .regularPrice(
                        regularPrice
                )
                .finalPrice(
                        finalPrice
                )
                .isDisplay(
                        requestDTO.getIsDisplay() != null
                                ? requestDTO.getIsDisplay()
                                : true
                )
                .imageUrl(
                        normalizeText(
                                requestDTO.getImageUrl()
                        )
                )
                .build();

        Product savedProduct =
                headProductMapper
                        .saveAndFlush(product);

        saveNewFlavorIfRequested(
                category,
                requestDTO
        );

        InventoryItem inventoryItem =
                InventoryItem.builder()
                        .product(savedProduct)
                        .unit(
                                resolveInventoryUnit(
                                        requestDTO.getInventoryUnit()
                                )
                        )
                        .unitPrice(
                                resolveInventoryUnitPrice(
                                        requestDTO,
                                        savedProduct
                                )
                        )
                        .build();

        InventoryItem savedItem =
                headInventoryItemMapper
                        .saveAndFlush(inventoryItem);

        Set<Integer> storeIds =
                new LinkedHashSet<>(
                        requestDTO.getStoreIds()
                );

        for (Integer storeId : storeIds) {

            if (storeId == null || storeId <= 0) {
                throw new IllegalArgumentException(
                        "잘못된 지점 ID입니다: "
                                + storeId
                );
            }

            Store store = headStoreMapper
                    .findById(storeId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "존재하지 않는 지점입니다. storeId="
                                            + storeId
                            )
                    );

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
                                .isSoldOut(false)
                                .isDeleted(false)
                                .build();

                headStoreProductMapper
                        .saveAndFlush(storeProduct);
            }

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

                headStoreInventoryMapper
                        .saveAndFlush(storeInventory);
            }
        }

        return toResponseDTO(savedProduct);
    }

    public List<HeadProductResponseDTO> getProductList() {

        return headProductMapper
                .findByIsDisplayTrueOrderByIdDesc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

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

    @Transactional
    public HeadProductResponseDTO updateProduct(
            Integer productId,
            HeadProductCreateRequestDTO requestDTO
    ) {

        validateUpdateRequest(requestDTO);

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

        Integer discountRate =
                resolveDiscountRate(
                        requestDTO.getDiscountRate()
                );

        Integer marginRate =
                resolveMarginRate(
                        requestDTO.getMarginRate()
                );

        validatePriceRates(
                discountRate,
                marginRate
        );

        Integer regularPrice =
                calculateRegularPrice(
                        requestDTO.getBasePrice(),
                        marginRate
                );

        Integer finalPrice =
                calculateFinalPrice(
                        regularPrice,
                        discountRate
                );

        product.updateProduct(
                category,
                requestDTO
                        .getProductName()
                        .trim(),
                normalizeText(
                        requestDTO.getDescription()
                ),
                requestDTO.getBasePrice(),
                discountRate,
                marginRate,
                regularPrice,
                finalPrice,
                requestDTO.getIsDisplay() != null
                        ? requestDTO.getIsDisplay()
                        : true,
                requestDTO.getImageUrl() != null
                        ? normalizeText(
                                requestDTO.getImageUrl()
                        )
                        : product.getImageUrl()
        );

        return toResponseDTO(product);
    }

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

    private void validateCreateRequest(
            HeadProductCreateRequestDTO requestDTO
    ) {

        validateCommonProductRequest(requestDTO);

        if (
                requestDTO.getStoreIds() == null ||
                requestDTO.getStoreIds().isEmpty()
        ) {
            throw new IllegalArgumentException(
                    "판매 지점을 한 개 이상 선택해주세요."
            );
        }
    }

    private void validateUpdateRequest(
            HeadProductCreateRequestDTO requestDTO
    ) {

        validateCommonProductRequest(requestDTO);
    }

    private void validateCommonProductRequest(
            HeadProductCreateRequestDTO requestDTO
    ) {

        if (requestDTO == null) {
            throw new IllegalArgumentException(
                    "상품 요청 정보가 없습니다."
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
    }

    private void validatePriceRates(
            Integer discountRate,
            Integer marginRate
    ) {

        if (
                discountRate < 0 ||
                discountRate > 100
        ) {
            throw new IllegalArgumentException(
                    "할인율은 0 이상 100 이하여야 합니다."
            );
        }

        if (
                marginRate < 0 ||
                marginRate >= 100
        ) {
            throw new IllegalArgumentException(
                    "마진율은 0 이상 100 미만이어야 합니다."
            );
        }
    }

    private HeadProductResponseDTO toResponseDTO(
            Product product
    ) {

        return HeadProductResponseDTO.builder()
                .productId(product.getId())
                .categoryId(
                        product.getCategory().getId()
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
                .marginRate(
                        product.getMarginRate()
                )
                .regularPrice(
                        product.getRegularPrice()
                )
                .discountRate(
                        product.getDiscountRate()
                )
                .finalPrice(
                        product.getFinalPrice()
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

    private void validateNewFlavorRequest(
            Category category,
            HeadProductCreateRequestDTO requestDTO
    ) {

        String flavorName =
                normalizeText(
                        requestDTO.getNewFlavorName()
                );

        String flavorImageUrl =
                normalizeText(
                        requestDTO.getNewFlavorImageUrl()
                );

        if (!supportsFlavor(category)) {
            return;
        }

        if (
                flavorName == null &&
                flavorImageUrl == null
        ) {
            return;
        }

        if (flavorName == null) {
            throw new IllegalArgumentException(
                    "추가할 맛 이름을 입력해주세요."
            );
        }

        if (flavorImageUrl == null) {
            throw new IllegalArgumentException(
                    "추가할 맛 이미지 URL을 입력해주세요."
            );
        }

        if (
                headFlavorMapper
                        .existsByFlavorName(
                                flavorName
                        )
        ) {
            throw new IllegalArgumentException(
                    "이미 등록된 아이스크림 맛입니다."
            );
        }
    }

    private void saveNewFlavorIfRequested(
            Category category,
            HeadProductCreateRequestDTO requestDTO
    ) {

        if (!supportsFlavor(category)) {
            return;
        }

        String flavorName =
                normalizeText(
                        requestDTO.getNewFlavorName()
                );

        String flavorImageUrl =
                normalizeText(
                        requestDTO.getNewFlavorImageUrl()
                );

        if (
                flavorName == null &&
                flavorImageUrl == null
        ) {
            return;
        }

        IcecreamFlavor flavor =
                IcecreamFlavor.builder()
                        .flavorName(flavorName)
                        .isActive(true)
                        .imageUrl(flavorImageUrl)
                        .build();

        headFlavorMapper.save(flavor);
    }

    private Integer calculateRegularPrice(
            Integer basePrice,
            Integer marginRate
    ) {

        if (basePrice == null || basePrice < 0) {
            throw new IllegalArgumentException(
                    "기본 가격은 0 이상이어야 합니다."
            );
        }

        if (
                marginRate == null ||
                marginRate < 0 ||
                marginRate >= 100
        ) {
            throw new IllegalArgumentException(
                    "마진율은 0 이상 100 미만이어야 합니다."
            );
        }

        if (basePrice == 0) {
            return 0;
        }

        double regularPrice =
                basePrice /
                (
                        1 -
                        marginRate / 100.0
                );

        return roundToHundred(
                regularPrice
        );
    }

    private Integer calculateFinalPrice(
            Integer regularPrice,
            Integer discountRate
    ) {

        if (
                regularPrice == null ||
                regularPrice < 0
        ) {
            throw new IllegalArgumentException(
                    "정가는 0 이상이어야 합니다."
            );
        }

        if (
                discountRate == null ||
                discountRate < 0 ||
                discountRate > 100
        ) {
            throw new IllegalArgumentException(
                    "할인율은 0 이상 100 이하여야 합니다."
            );
        }

        double finalPrice =
                regularPrice *
                (
                        1 -
                        discountRate / 100.0
                );

        return roundToHundred(
                finalPrice
        );
    }

    private Integer roundToHundred(
            double price
    ) {

        return (int) (
                Math.round(
                        price / 100.0
                ) * 100
        );
    }

    private Integer resolveDiscountRate(
            Integer discountRate
    ) {

        return discountRate != null
                ? discountRate
                : DEFAULT_DISCOUNT_RATE;
    }

    private Integer resolveMarginRate(
            Integer marginRate
    ) {

        return marginRate != null
                ? marginRate
                : DEFAULT_MARGIN_RATE;
    }

    private String resolveInventoryUnit(
            String inventoryUnit
    ) {

        String normalizedUnit =
                normalizeText(inventoryUnit);

        return normalizedUnit != null
                ? normalizedUnit
                : "EA";
    }

    private Integer resolveInventoryUnitPrice(
            HeadProductCreateRequestDTO requestDTO,
            Product savedProduct
    ) {

        Integer inventoryUnitPrice =
                requestDTO.getInventoryUnitPrice();

        if (
                inventoryUnitPrice != null &&
                inventoryUnitPrice >= 0
        ) {
            return inventoryUnitPrice;
        }

        return savedProduct.getBasePrice();
    }

    private String normalizeText(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}