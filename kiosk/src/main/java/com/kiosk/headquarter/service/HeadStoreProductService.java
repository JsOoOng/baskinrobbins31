package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Product;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.StoreProduct;
import com.kiosk.headquarter.dto.store.HeadStoreProductAddRequestDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductDetailResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductListResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadInventoryItemMapper;
import com.kiosk.headquarter.repository.HeadProductMapper;
import com.kiosk.headquarter.repository.HeadStoreInventoryMapper;
import com.kiosk.headquarter.repository.HeadStoreMapper;
import com.kiosk.headquarter.repository.HeadStoreProductMapper;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadStoreProductService
 *
 * <p>역할: 본사 관리의 지점 판매 상품 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadStoreProductMapper, HeadStoreMapper, HeadProductMapper, HeadInventoryItemMapper 등 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStoreProductService {

    private final HeadStoreProductMapper
            headStoreProductMapper;

    private final HeadStoreMapper
            headStoreMapper;

    private final HeadProductMapper
            headProductMapper;
    
    private final HeadInventoryItemMapper
    	headInventoryItemMapper;
    
    private final HeadStoreInventoryMapper
    	headStoreInventoryMapper;
    private final AdminLogService adminLogService;

    /*
     * 지점 판매 메뉴 등록
     */
    @Transactional
    /**
     * [메서드 흐름] addStoreProduct
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreProductMapper, HeadStoreMapper, HeadProductMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String addStoreProduct(
            Integer storeId,
            HeadStoreProductAddRequestDTO requestDTO
    ) {

        if (requestDTO == null) {
            throw new IllegalArgumentException(
                    "등록 정보를 입력해주세요."
            );
        }

        if (requestDTO.getProductId() == null) {
            throw new IllegalArgumentException(
                    "상품 번호가 필요합니다."
            );
        }

        Store store =
                headStoreMapper.findById(storeId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "존재하지 않는 지점입니다."
                                        )
                        );

        Product product =
                headProductMapper
                        .findById(
                                requestDTO.getProductId()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "존재하지 않는 상품입니다."
                                        )
                        );

        boolean alreadyExists =
                headStoreProductMapper
                        .existsByStore_IdAndProduct_IdAndIsDeletedFalse(
                                storeId,
                                requestDTO.getProductId()
                        );

        if (alreadyExists) {
            throw new IllegalArgumentException(
                    "이미 해당 지점에 등록된 상품입니다."
            );
        }

        /*
         * 지점 가격이 없으면
         * 본사 기본 가격을 사용합니다.
         */
        Integer storeProductPrice =
                requestDTO.getStoreProductPrice();

        if (storeProductPrice == null) {
            storeProductPrice =
                    product.getBasePrice();
        }

        validatePrice(storeProductPrice);

        StoreProduct storeProduct =
                StoreProduct.builder()
                        .store(store)
                        .product(product)
                        .storeProductPrice(storeProductPrice)
                        .isSoldOut(
                                Boolean.TRUE.equals(
                                        requestDTO.getIsSoldOut()
                                )
                        )
                        .isDeleted(false)
                        .build();

        headStoreProductMapper.save(storeProduct);

        InventoryItem item =
                headInventoryItemMapper.findByProduct(product)
                        .orElseThrow(() ->
                                new IllegalArgumentException("재고 품목이 존재하지 않습니다."));

        // 이미 재고가 등록되어 있는지 확인
        if (!headStoreInventoryMapper.existsByStoreAndItem(store, item)) {

            StoreInventory inventory =
                    StoreInventory.builder()
                            .store(store)
                            .item(item)
                            .currentStock(10)
                            .build();

            headStoreInventoryMapper.save(inventory);
        }

        adminLogService.logAction("지점 상품",
                store.getStoreName() + " - " + product.getProductName() + " 판매 상품 등록");
        return "지점 판매 메뉴 등록 성공";
    }

    /*
     * 지점 판매 메뉴 목록 조회
     */
    /**
     * [메서드 흐름] getStoreProductList
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreProductMapper, HeadStoreMapper, HeadProductMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadStoreProductListResponseDTO>
            getStoreProductList(
                    Integer storeId
            ) {

        /*
         * 존재하지 않는 지점 ID로 요청해도
         * 빈 배열만 반환되는 것을 방지합니다.
         */
        if (!headStoreMapper.existsById(storeId)) {
            throw new IllegalArgumentException(
                    "존재하지 않는 지점입니다."
            );
        }

        return headStoreProductMapper
                .findByStore_IdAndIsDeletedFalseOrderByIdDesc(
                        storeId
                )
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    /*
     * 지점 판매 메뉴 상세 조회
     */
    /**
     * [메서드 흐름] getStoreProductDetail
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreProductMapper, HeadStoreMapper, HeadProductMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadStoreProductDetailResponseDTO
            getStoreProductDetail(
                    Integer storeId,
                    Integer storeProductId
            ) {

        StoreProduct storeProduct =
                findStoreProduct(
                        storeId,
                        storeProductId
                );

        return toDetailResponseDTO(
                storeProduct
        );
    }

    /*
     * 지점 판매 메뉴 수정
     */
    @Transactional
    /**
     * [메서드 흐름] updateStoreProduct
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreProductMapper, HeadStoreMapper, HeadProductMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String updateStoreProduct(
            Integer storeId,
            Integer storeProductId,
            HeadStoreProductUpdateRequestDTO requestDTO
    ) {

        if (requestDTO == null) {
            throw new IllegalArgumentException(
                    "수정 정보를 입력해주세요."
            );
        }

        StoreProduct storeProduct =
                findStoreProduct(
                        storeId,
                        storeProductId
                );

        Boolean isSoldOut =
                requestDTO.getIsSoldOut() != null
                        ? requestDTO.getIsSoldOut()
                        : storeProduct.getIsSoldOut();

        Integer storeProductPrice =
                requestDTO.getStoreProductPrice() != null
                        ? requestDTO.getStoreProductPrice()
                        : storeProduct.getStoreProductPrice();

        validatePrice(storeProductPrice);
        storeProduct.changeStoreProductPrice(storeProductPrice);
        storeProduct.changeSoldOut(isSoldOut);

        adminLogService.logAction("지점 상품",
                storeProduct.getStore().getStoreName() + " - "
                        + storeProduct.getProduct().getProductName() + " 판매 정보 수정");
        return "지점 판매 메뉴 수정 성공";
    }

    /*
     * 지점 판매 메뉴 삭제
     *
     * 실제 DB 행을 지우지 않고
     * isDeleted를 true로 변경합니다.
     */
    @Transactional
    /**
     * [메서드 흐름] deleteStoreProduct
     * Controller 또는 상위 서비스에서 호출되어 HeadStoreProductMapper, HeadStoreMapper, HeadProductMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String deleteStoreProduct(
            Integer storeId,
            Integer storeProductId
    ) {

        StoreProduct storeProduct =
                findStoreProduct(
                        storeId,
                        storeProductId
                );

        storeProduct.deleteStoreProduct();

        adminLogService.logAction("지점 상품",
                storeProduct.getStore().getStoreName() + " - "
                        + storeProduct.getProduct().getProductName() + " 판매 상품 삭제");
        return "지점 판매 메뉴 삭제 성공";
    }

    /*
     * 판매 상품 공통 조회
     */
    private StoreProduct findStoreProduct(
            Integer storeId,
            Integer storeProductId
    ) {

        return headStoreProductMapper
                .findByStore_IdAndIdAndIsDeletedFalse(
                        storeId,
                        storeProductId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "존재하지 않는 지점 판매 상품입니다."
                                )
                );
    }

    /*
     * 가격 검증
     */
    private void validatePrice(
            Integer price
    ) {

        if (price == null || price < 0) {
            throw new IllegalArgumentException(
                    "판매 가격은 0원 이상이어야 합니다."
            );
        }
    }

    /*
     * 목록 응답 변환
     */
    private HeadStoreProductListResponseDTO
            toListResponseDTO(
                    StoreProduct storeProduct
            ) {

        return HeadStoreProductListResponseDTO
                .builder()
                .storeProductId(
                        storeProduct.getId()
                )
                .storeId(
                        storeProduct
                                .getStore()
                                .getId()
                )
                .productId(
                        storeProduct
                                .getProduct()
                                .getId()
                )
                .productName(
                        storeProduct
                                .getProduct()
                                .getProductName()
                )
                .basePrice(
                        storeProduct
                                .getProduct()
                                .getBasePrice()
                )
                .storeProductPrice(
                        storeProduct.getStoreProductPrice()
                )
                .isSoldOut(
                        storeProduct.getIsSoldOut()
                )
                .build();
    }

    /*
     * 상세 응답 변환
     */
    private HeadStoreProductDetailResponseDTO
            toDetailResponseDTO(
                    StoreProduct storeProduct
            ) {

        Product product =
                storeProduct.getProduct();

        return HeadStoreProductDetailResponseDTO
                .builder()
                .storeProductId(
                        storeProduct.getId()
                )
                .storeId(
                        storeProduct
                                .getStore()
                                .getId()
                )
                .productId(
                        product.getId()
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
                .storeProductPrice(
                        storeProduct.getStoreProductPrice()
                )
                .discountRate(
                        product.getDiscountRate()
                )
                .isDisplay(
                        product.getIsDisplay()
                )
                .isSoldOut(
                        storeProduct.getIsSoldOut()
                )
                .build();
    }
}
