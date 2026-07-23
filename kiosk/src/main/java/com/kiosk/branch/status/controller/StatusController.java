package com.kiosk.branch.status.controller;


import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.status.dto.AddFlavorRequest;
import com.kiosk.branch.status.dto.ContainerUpdateRequest;
import com.kiosk.branch.status.dto.FlavorResponse;
import com.kiosk.branch.status.dto.SoldOutRequest;
import com.kiosk.branch.status.dto.StoreFlavorRestockRequest;
import com.kiosk.branch.status.dto.StoreFlavorStatusResponse;
import com.kiosk.branch.status.dto.StoreProductStatusResponse;
import com.kiosk.branch.status.service.StatusService;

import lombok.RequiredArgsConstructor;
/**
 * [코드 흐름 안내] StatusController
 *
 * <p>역할: 지점 운영의 재고 상태·재입고 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch/status) -> StatusService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/branch/status")
@RequiredArgsConstructor
public class StatusController {


    private final StatusService statusService;



    // 품절 처리
    /**
     * [요청 흐름] PATCH /branch/status/product/{storeProductId}
     * 프론트 요청을 받아 updateProductSoldOut() 메서드가 입력을 받고 StatusService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/product/{storeProductId}")
    public StoreProductStatusResponse updateProductSoldOut(
            @PathVariable Integer storeProductId,
            @RequestBody SoldOutRequest request
    ){

        return statusService.updateProductSoldOut(
                storeProductId,
                request.getSoldOut()
        );

    }
    
    /**
     * [요청 흐름] PATCH /branch/status/flavor/{storeFlavorId}
     * 프론트 요청을 받아 updateFlavorSoldOut() 메서드가 입력을 받고 StatusService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/flavor/{storeFlavorId}")
    public StoreFlavorStatusResponse updateFlavorSoldOut(
            @PathVariable Integer storeFlavorId,
            @RequestBody SoldOutRequest request
    ){

        return statusService.updateFlavorSoldOut(
        		storeFlavorId,
                request.getSoldOut()
        );

    }



    // 지점 메뉴 조회
    /**
     * [요청 흐름] GET /branch/status/product/{storeId}
     * 프론트 요청을 받아 getProducts() 메서드가 입력을 받고 StatusService 호출 후 결과를 응답한다.
     */
    @GetMapping("/product/{storeId}")
    public List<StoreProductStatusResponse> getProducts(
            @PathVariable Integer storeId
    ){

        return statusService.getProducts(storeId);

    }
    
 // 지점 맛 조회
    /**
     * [요청 흐름] GET /branch/status/flavor/{storeId}
     * 프론트 요청을 받아 getFlavors() 메서드가 입력을 받고 StatusService 호출 후 결과를 응답한다.
     */
    @GetMapping("/flavor/{storeId}")
    public List<StoreFlavorStatusResponse> getFlavors(
            @PathVariable Integer storeId
    ){

        return statusService.getFlavors(storeId);

    }
    
    /**
     * [요청 흐름] GET /branch/status/all-flavors
     * 프론트 요청을 받아 getAllFlavors() 메서드가 입력을 받고 StatusService 호출 후 결과를 응답한다.
     */
    @GetMapping("/all-flavors")
    public List<FlavorResponse> getAllFlavors(){

        return statusService.getAllFlavors();

    }
    
    /**
     * [요청 흐름] POST /branch/status/flavor/{storeId}
     * 프론트 요청을 받아 addFlavor() 메서드가 입력을 받고 StatusService 호출 후 결과를 응답한다.
     */
    @PostMapping("/flavor/{storeId}")
    public StoreFlavorStatusResponse addFlavor(

            @PathVariable Integer storeId,

            @RequestBody AddFlavorRequest request

    ){

        return statusService.addFlavor(
                storeId,
                request.getFlavorId()
        );

    }
    
    /**
     * [요청 흐름] DELETE /branch/status/flavor/{storeFlavorId}
     * 프론트 요청을 받아 deleteFlavor() 메서드가 입력을 받고 StatusService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/flavor/{storeFlavorId}")
    public void deleteFlavor(
            @PathVariable Integer storeFlavorId
    ){

        statusService.deleteFlavor(storeFlavorId);

    }
    
    /**
     * [요청 흐름] PATCH /branch/status/flavor/{storeFlavorId}/container
     * 프론트 요청을 받아 updateContainer() 메서드가 입력을 받고 StatusService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/flavor/{storeFlavorId}/container")
    public StoreFlavorStatusResponse updateContainer(

            @PathVariable Integer storeFlavorId,

            @RequestBody ContainerUpdateRequest request

    ){


        return statusService.updateContainer(
                storeFlavorId,
                request.getAmount()
        );

    }
    
    /**
     * [요청 흐름] PATCH /branch/status/flavor/{storeFlavorId}/restock
     * 프론트 요청을 받아 updateFlavorRestock() 메서드가 입력을 받고 StatusService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/flavor/{storeFlavorId}/restock")
    public StoreFlavorStatusResponse updateFlavorRestock(
            @PathVariable Integer storeFlavorId,
            @RequestBody StoreFlavorRestockRequest request
    ){

        return statusService
                .updateFlavorRestockSetting(
                        storeFlavorId,
                        request
                );

    }

}