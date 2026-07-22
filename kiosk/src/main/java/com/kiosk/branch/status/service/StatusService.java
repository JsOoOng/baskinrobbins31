package com.kiosk.branch.status.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.inventory.repository.BranchInventoryMapper;
import com.kiosk.branch.status.dto.FlavorResponse;
import com.kiosk.branch.status.dto.StoreFlavorRestockRequest;
import com.kiosk.branch.status.dto.StoreFlavorStatusResponse;
import com.kiosk.branch.status.dto.StoreProductStatusResponse;
import com.kiosk.branch.status.reopsitory.IcecreamFlavorMapper;
import com.kiosk.branch.status.reopsitory.RestockRequestMapper;
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

    private final RestockRequestMapper restockRequestMapper;



    /*
     * 상품 수동 품절 변경
     */
    public StoreProductStatusResponse updateProductSoldOut(
            Integer storeProductId,
            Boolean soldOut
    ){

        StoreProduct storeProduct =
                storeProductMapper.findById(storeProductId)
                .orElseThrow(() ->
                        new IllegalArgumentException("상품 없음")
                );


        /*
         * 점장 수동 품절 상태 변경
         */
        storeProduct.changeManualSoldOut(soldOut);



        StoreInventory inventory =
                storeInventoryMapper
                .findByStoreIdAndItemProductId(
                        storeProduct.getStore().getId(),
                        storeProduct.getProduct().getId()
                )
                .orElse(null);



        Integer stock =
                inventory == null
                ? 0
                : inventory.getCurrentStock();



        Integer storeInventoryId =
                inventory == null
                ? null
                : inventory.getId();



        /*
         * 최종 품절 상태
         * 수동 품절 OR 재고 없음
         */
        Boolean finalSoldOut =
                storeProduct.getManualSoldOut()
                ||
                stock <= 0;



        return StoreProductStatusResponse
                .from(
                        storeProduct,
                        storeInventoryId,
                        stock,
                        finalSoldOut
                );

    }






    /*
     * 상품 조회
     */
    @Transactional(readOnly = true)
    public List<StoreProductStatusResponse> getProducts(
            Integer storeId
    ){

        return storeProductMapper
                .findByStoreId(storeId)
                .stream()
                .map(sp -> {


                    boolean autoSoldOut = false;

                    int totalStock = 0;

                    Integer storeInventoryId = null;



                    for(InventoryItem item :
                            sp.getProduct().getInventoryItems()){


                        StoreInventory inventory =
                                inventoryMapper
                                .findByStore_IdAndItem_Id(
                                        storeId,
                                        item.getId()
                                )
                                .orElse(null);



                        Integer stock =
                                inventory == null
                                ? 0
                                : inventory.getCurrentStock();



                        if(inventory != null){

                            storeInventoryId =
                                    inventory.getId();

                        }



                        totalStock += stock;



                        /*
                         * 자동 품절
                         */
                        if(stock <= 0){

                            autoSoldOut = true;

                        }

                    }



                    /*
                     * 최종 품절
                     */
                    Boolean finalSoldOut =
                            sp.getManualSoldOut()
                            ||
                            autoSoldOut;



                    return StoreProductStatusResponse
                            .from(
                                    sp,
                                    storeInventoryId,
                                    totalStock,
                                    finalSoldOut
                            );


                })
                .toList();

    }







    /*
     * 맛 품절 변경
     */
    public StoreFlavorStatusResponse updateFlavorSoldOut(
            Integer storeFlavorId,
            Boolean soldOut
    ){

        StoreFlavor storeFlavor =
                storeFlavorMapper.findById(storeFlavorId)
                .orElseThrow(() ->
                        new IllegalArgumentException("맛 없음")
                );


        storeFlavor.changeSoldOut(soldOut);



        return StoreFlavorStatusResponse
                .from(storeFlavor);

    }







    /*
     * 맛 조회
     */
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
                .findByIsActiveTrue()
                .stream()
                .map(FlavorResponse::from)
                .toList();

    }







    /*
     * 맛 추가
     */
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







    /*
     * 맛 삭제
     */
    @Transactional
    public void deleteFlavor(Integer storeFlavorId)
    {

        StoreFlavor storeFlavor =
                storeFlavorMapper.findById(storeFlavorId)
                .orElseThrow(
                        () -> new IllegalArgumentException("등록된 맛 없음")
                );


        restockRequestMapper.deleteByStoreFlavorId(storeFlavorId);


        storeFlavorMapper.delete(storeFlavor);

    }







    /*
     * 맛 통 수 변경
     */
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



        if(storeFlavor.getContainer() <= 0){

            storeFlavor.changeSoldOut(true);

        } else {

            storeFlavor.changeSoldOut(false);

        }



        return StoreFlavorStatusResponse
                .from(storeFlavor);

    }







    /*
     * 맛 자동 발주 설정
     */
    @Transactional
    public StoreFlavorStatusResponse updateFlavorRestockSetting(
            Integer storeFlavorId,
            StoreFlavorRestockRequest request
    ){

        StoreFlavor storeFlavor =
                storeFlavorMapper.findById(storeFlavorId)
                .orElseThrow(
                        () -> new IllegalArgumentException("맛 없음")
                );



        storeFlavor.updateAutoRestockSetting(
                request.getMinStock(),
                request.getTargetStock(),
                request.getAutoRestockEnabled(),
                request.getRestockMode()
        );



        return StoreFlavorStatusResponse
                .from(storeFlavor);

    }

}