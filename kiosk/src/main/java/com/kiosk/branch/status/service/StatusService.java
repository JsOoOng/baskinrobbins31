package com.kiosk.branch.status.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.status.dto.StoreFlavorStatusResponse;
import com.kiosk.branch.status.dto.StoreProductStatusResponse;
import com.kiosk.branch.status.reopsitory.StoreFlavorMapper;
import com.kiosk.branch.status.reopsitory.StoreProductMapper;
import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.StoreProduct;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class StatusService {


    private final StoreProductMapper storeProductMapper;
    private final StoreFlavorMapper storeFlavorMapper;

    

    // 지점 메뉴 품절 상태 변경
    public StoreProductStatusResponse updateProductSoldOut(
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
                .findByStoreId(storeId)
                .stream()
                .map(StoreProductStatusResponse::from)
                .toList();

    }



	public StoreFlavorStatusResponse updateFlavorSoldOut(Integer storeFlavorId, Boolean soldOut) {
		    StoreFlavor storeFlavor =
		    		storeFlavorMapper.findById(storeFlavorId)
		            .orElseThrow(() -> 
		                new IllegalArgumentException("맛 없음")
		            );
		
		    storeFlavor.changeSoldOut(soldOut);
		
		
		    return StoreFlavorStatusResponse
		            .from(storeFlavor);
		}
	
	// 지점 맛 상태 조회
	@Transactional(readOnly = true)
	public List<StoreFlavorStatusResponse> getFlavors(
	        Integer storeId
	){

	    return storeFlavorMapper
	            .findByStoreId(storeId)
	            .stream()
	            .map(StoreFlavorStatusResponse::from)
	            .toList();

	}
	

}