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


/**
 * [코드 흐름 안내] StatusService
 *
 * <p>역할: 지점 운영의 재고 상태·재입고 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper, StoreMapper 등 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
    /**
     * [메서드 흐름] updateProductSoldOut
     * Controller 또는 상위 서비스에서 호출되어 StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
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
    /**
     * [메서드 흐름] getProducts
     * Controller 또는 상위 서비스에서 호출되어 StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] updateFlavorSoldOut
     * Controller 또는 상위 서비스에서 호출되어 StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
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
    /**
     * [메서드 흐름] getFlavors
     * Controller 또는 상위 서비스에서 호출되어 StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] getAllFlavors
     * Controller 또는 상위 서비스에서 호출되어 StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] addFlavor
     * Controller 또는 상위 서비스에서 호출되어 StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
                .container(5)
                .build();



        storeFlavorMapper.save(storeFlavor);



        return StoreFlavorStatusResponse
                .from(storeFlavor);

    }







    /*
     * 맛 삭제
     */
    @Transactional
    /**
     * [메서드 흐름] deleteFlavor
     * Controller 또는 상위 서비스에서 호출되어 StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] updateContainer
     * Controller 또는 상위 서비스에서 호출되어 StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
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
    /**
     * [메서드 흐름] updateFlavorRestockSetting
     * Controller 또는 상위 서비스에서 호출되어 StoreProductMapper, StoreFlavorMapper, IcecreamFlavorMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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