package com.kiosk.branch.status.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.status.dto.FlavorResponse;
import com.kiosk.branch.status.dto.StoreFlavorStatusResponse;
import com.kiosk.branch.status.dto.StoreProductStatusResponse;
import com.kiosk.branch.status.reopsitory.IcecreamFlavorMapper;
import com.kiosk.branch.status.reopsitory.StoreFlavorMapper;
import com.kiosk.branch.status.reopsitory.StoreMapper;
import com.kiosk.branch.status.reopsitory.StoreProductMapper;
import com.kiosk.entity.IcecreamFlavor;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.StoreProduct;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class StatusService {


    private final StoreProductMapper storeProductMapper;
    private final StoreFlavorMapper storeFlavorMapper;
    private final IcecreamFlavorMapper icecreamFlavorMapper;
    private final StoreMapper storeMapper;

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
	
	
	@Transactional(readOnly = true)
	public List<FlavorResponse> getAllFlavors(){


	    return icecreamFlavorMapper
	            .findByIsActiveTrue() //.findAll()
	            .stream()
	            .map(FlavorResponse::from)
	            .toList();

	}
	
	@Transactional
	public StoreFlavorStatusResponse addFlavor(
	        Integer storeId,
	        Integer flavorId
	){


	    if(storeFlavorMapper.existsByStoreIdAndFlavorId(
	            storeId,
	            flavorId
	    )){

	        throw new IllegalArgumentException(
	                "이미 등록된 맛입니다."
	        );

	    }



	    IcecreamFlavor flavor =
	            icecreamFlavorMapper.findById(flavorId)
	            .orElseThrow(
	                    () -> new IllegalArgumentException("맛 없음")
	            );



	    Store store =
	            storeMapper.findById(storeId)
	            .orElseThrow(
	                    () -> new IllegalArgumentException("지점 없음")
	            );



	    StoreFlavor storeFlavor =
	            StoreFlavor.builder()
	            .store(store)
	            .flavor(flavor)
	            .build();



	    storeFlavorMapper.save(storeFlavor);



	    return StoreFlavorStatusResponse
	            .from(storeFlavor);

	}
	
	@Transactional
	public void deleteFlavor(Integer storeFlavorId){


	    StoreFlavor storeFlavor =
	            storeFlavorMapper.findById(storeFlavorId)
	            .orElseThrow(
	                () -> new IllegalArgumentException("등록된 맛 없음")
	            );


	    storeFlavorMapper.delete(storeFlavor);

	}
	
	public StoreFlavorStatusResponse updateContainer(
	        Integer storeFlavorId,
	        Integer amount
	){


	    StoreFlavor storeFlavor =
	            storeFlavorMapper.findById(storeFlavorId)
	            .orElseThrow(() ->
	                new IllegalArgumentException("맛 없음")
	            );


	    storeFlavor.changeContainer(amount);


	    return StoreFlavorStatusResponse
	            .from(storeFlavor);

	}

}