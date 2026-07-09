package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Product;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreProduct;
import com.kiosk.headquarter.dto.store.HeadStoreProductAddRequestDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductDetailResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductListResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadProductMapper;
import com.kiosk.headquarter.repository.HeadStoreMapper;
import com.kiosk.headquarter.repository.HeadStoreProductMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStoreProductService {

    private final HeadStoreProductMapper headStoreProductMapper;
    private final HeadStoreMapper headStoreMapper;
    private final HeadProductMapper headProductMapper;

    // 지점 판매 메뉴 등록
    @Transactional
    public String addStoreProduct(Integer storeId, HeadStoreProductAddRequestDTO requestDTO) {

        Store store = headStoreMapper.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점입니다."));

        Product product = headProductMapper.findById(requestDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        boolean alreadyExists = headStoreProductMapper.existsByStore_IdAndProduct_Id(
                storeId,
                requestDTO.getProductId()
        );

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 해당 지점에 등록된 상품입니다.");
        }

        StoreProduct storeProduct = StoreProduct.builder()
                .store(store)
                .product(product)
                .isSoldOut(
                        requestDTO.getIsSoldOut() != null
                                ? requestDTO.getIsSoldOut()
                                : false
                )
                .build();

        headStoreProductMapper.save(storeProduct);

        return "지점 판매 메뉴 등록 성공";
    }

    // 지점 판매 메뉴 목록 조회
    public List<HeadStoreProductListResponseDTO> getStoreProductList(Integer storeId) {

        return headStoreProductMapper.findByStore_IdOrderByIdDesc(storeId)
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }

    // 지점 판매 메뉴 상세 조회
    public HeadStoreProductDetailResponseDTO getStoreProductDetail(
            Integer storeId,
            Integer storeProductId) {

        StoreProduct storeProduct = headStoreProductMapper.findByStore_IdAndId(
                        storeId,
                        storeProductId
                )
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점 판매 상품입니다."));

        return toDetailResponseDTO(storeProduct);
    }

    // 지점 판매 메뉴 수정
    @Transactional
    public String updateStoreProduct(
            Integer storeId,
            Integer storeProductId,
            HeadStoreProductUpdateRequestDTO requestDTO) {

        StoreProduct storeProduct = headStoreProductMapper.findByStore_IdAndId(
                        storeId,
                        storeProductId
                )
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점 판매 상품입니다."));

        storeProduct.updateSoldOut(
                requestDTO.getIsSoldOut() != null
                        ? requestDTO.getIsSoldOut()
                        : false
        );

        return "지점 판매 메뉴 수정 성공";
    }

    // 지점 판매 메뉴 삭제
    @Transactional
    public String deleteStoreProduct(Integer storeId, Integer storeProductId) {

        StoreProduct storeProduct = headStoreProductMapper.findByStore_IdAndId(
                        storeId,
                        storeProductId
                )
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지점 판매 상품입니다."));

        headStoreProductMapper.delete(storeProduct);

        return "지점 판매 메뉴 삭제 성공";
    }

    private HeadStoreProductListResponseDTO toListResponseDTO(StoreProduct storeProduct) {
        return HeadStoreProductListResponseDTO.builder()
                .storeProductId(storeProduct.getId())
                .storeId(storeProduct.getStore().getId())
                .productId(storeProduct.getProduct().getId())
                .productName(storeProduct.getProduct().getProductName())
                .basePrice(storeProduct.getProduct().getBasePrice())
                .isSoldOut(storeProduct.getIsSoldOut())
                .build();
    }

    private HeadStoreProductDetailResponseDTO toDetailResponseDTO(StoreProduct storeProduct) {
        return HeadStoreProductDetailResponseDTO.builder()
                .storeProductId(storeProduct.getId())
                .storeId(storeProduct.getStore().getId())
                .productId(storeProduct.getProduct().getId())
                .productName(storeProduct.getProduct().getProductName())
                .description(storeProduct.getProduct().getDescription())
                .basePrice(storeProduct.getProduct().getBasePrice())
                .discountRate(storeProduct.getProduct().getDiscountRate())
                .isDisplay(storeProduct.getProduct().getIsDisplay())
                .isSoldOut(storeProduct.getIsSoldOut())
                .build();
    }
}