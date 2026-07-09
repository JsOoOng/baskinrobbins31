package com.kiosk.branch.status.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.status.dto.StoreProductStatusResponse;
import com.kiosk.branch.status.reopsitory.StoreProductMapper;
import com.kiosk.entity.StoreProduct;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class StatusService {


    private final StoreProductMapper storeProductMapper;



    // 지점 메뉴 품절 상태 변경
    public StoreProductStatusResponse updateSoldOut(
            Integer storeProductId,
            Boolean soldOut
    ){

        StoreProduct storeProduct =
        		storeProductMapper.findById(storeProductId)
                .orElseThrow(() -> 
                    new IllegalArgumentException("상품 없음")
                );


        storeProduct.changeSoldOut(soldOut);


        return StoreProductStatusResponse
                .from(storeProduct);
    }



    // 지점 메뉴 상태 조회
    @Transactional(readOnly = true)
    public List<StoreProductStatusResponse> getProducts(
            Integer storeId
    ){

        return storeProductMapper
                .findByStoreStoreId(storeId)
                .stream()
                .map(StoreProductStatusResponse::from)
                .toList();

    }

}