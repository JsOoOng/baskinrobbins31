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
@RestController
@RequestMapping("/branch/status")
@RequiredArgsConstructor
public class StatusController {


    private final StatusService statusService;



    // 품절 처리
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
    @GetMapping("/product/{storeId}")
    public List<StoreProductStatusResponse> getProducts(
            @PathVariable Integer storeId
    ){

        return statusService.getProducts(storeId);

    }
    
 // 지점 맛 조회
    @GetMapping("/flavor/{storeId}")
    public List<StoreFlavorStatusResponse> getFlavors(
            @PathVariable Integer storeId
    ){

        return statusService.getFlavors(storeId);

    }
    
    @GetMapping("/all-flavors")
    public List<FlavorResponse> getAllFlavors(){

        return statusService.getAllFlavors();

    }
    
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
    
    @DeleteMapping("/flavor/{storeFlavorId}")
    public void deleteFlavor(
            @PathVariable Integer storeFlavorId
    ){

        statusService.deleteFlavor(storeFlavorId);

    }
    
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