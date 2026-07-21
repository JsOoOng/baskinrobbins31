package com.kiosk.branch.status.service;


import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.inventory.repository.BranchInventoryMapper;
import com.kiosk.branch.status.dto.FlavorResponse;
import com.kiosk.branch.status.dto.StoreFlavorStatusResponse;
import com.kiosk.branch.status.dto.StoreProductStatusResponse;
import com.kiosk.branch.status.reopsitory.IcecreamFlavorMapper;
import com.kiosk.branch.status.reopsitory.StoreFlavorMapper;
import com.kiosk.branch.status.reopsitory.StoreInventoryMapper;
import com.kiosk.branch.status.reopsitory.StoreMapper;
import com.kiosk.branch.status.reopsitory.StoreProductMapper;
import com.kiosk.entity.IcecreamFlavor;
import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.StoreInventory;
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
    private final StoreInventoryMapper storeInventoryMapper;
    private final BranchInventoryMapper inventoryMapper;
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


        Integer stock =
                storeInventoryMapper
                        .findByStoreIdAndItemProductId(
                                storeProduct.getStore().getId(),
                                storeProduct.getProduct().getId()
                        )
                        .map(StoreInventory::getCurrentStock)
                        .orElse(0);


        return StoreProductStatusResponse
                .from(
                        storeProduct,
                        stock
                );
    }



    // 지점 메뉴 상태 조회
    @Transactional
    public List<StoreProductStatusResponse> getProducts(
            Integer storeId
    ){

        return storeProductMapper
                .findByStoreId(storeId)
                .stream()
                .map(sp -> {


                    boolean soldOut = false;

                    int totalStock = 0;


                    for(InventoryItem item : sp.getProduct().getInventoryItems()){


                        Integer stock =
                        		inventoryMapper
                                .findByStore_IdAndItem_Id(
                                        storeId,
                                        item.getId()
                                )
                                .map(StoreInventory::getCurrentStock)
                                .orElse(0);



                        totalStock += stock;



                        // 하나라도 0이면 품절
                        if(stock <= 0){

                            soldOut = true;

                        }

                    }



                    sp.changeSoldOut(soldOut);



                    return StoreProductStatusResponse
                            .from(
                                sp,
                                totalStock
                            );

                })
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
	    
	 // 컨테이너 0이면 품절 처리
	    if(storeFlavor.getContainer() <= 0){

	        storeFlavor.changeSoldOut(true);

	    } else {

	        // 재입고 시 자동 판매 가능
	        storeFlavor.changeSoldOut(false);

	    }


	    return StoreFlavorStatusResponse
	            .from(storeFlavor);

	}

}