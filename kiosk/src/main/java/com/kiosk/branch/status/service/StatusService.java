package com.kiosk.branch.status.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.status.dto.StoreProductStatusResponse;
import com.kiosk.branch.status.reopsitory.StoreProductRepository;
import com.kiosk.entity.StoreProduct;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class StatusService {


    private final StoreProductRepository storeProductRepository;



    // 지점 메뉴 품절 상태 변경
    public StoreProductStatusResponse updateSoldOut(
            Integer storeProductId,
            Boolean soldOut
    ){

        StoreProduct storeProduct =
                storeProductRepository.findById(storeProductId)
                .orElseThrow(() -> 
                    new IllegalArgumentException("상품 없음")
                );


        storeProduct.setIsSoldOut(soldOut);


        return StoreProductStatusResponse
                .from(storeProduct);
    }



    // 지점 메뉴 상태 조회
    @Transactional(readOnly = true)
    public List<StoreProductStatusResponse> getProducts(
            Integer storeId
    ){

        return storeProductRepository
                .findByStoreStoreId(storeId)
                .stream()
                .map(StoreProductStatusResponse::from)
                .toList();

    }

}