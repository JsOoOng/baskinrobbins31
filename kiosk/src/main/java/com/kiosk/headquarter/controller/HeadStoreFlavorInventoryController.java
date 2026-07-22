package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.storeFlavor.HeadStoreFlavorInventoryResponse;
import com.kiosk.headquarter.dto.storeFlavor.UpdateStoreFlavorRestockRequest;
import com.kiosk.headquarter.service.HeadStoreFlavorInventoryService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/head/flavor-inventory")
@RequiredArgsConstructor
public class HeadStoreFlavorInventoryController {


    private final HeadStoreFlavorInventoryService service;



    /*
     * 전체 지점 맛 재고 조회
     */
    @GetMapping
    public ResponseEntity<List<HeadStoreFlavorInventoryResponse>> getInventories() {


        return ResponseEntity.ok(
                service.getStoreFlavorInventories()
        );

    }



    /*
     * 특정 지점 맛 재고 조회
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<HeadStoreFlavorInventoryResponse>> getStoreInventories(
            @PathVariable Integer storeId
    ) {


        return ResponseEntity.ok(
                service.getStoreFlavorInventories(storeId)
        );

    }



    /*
     * 자동 보충 설정 수정
     */
    @PatchMapping("/{storeFlavorId}")
    public ResponseEntity<Void> updateRestockSetting(
            @PathVariable Integer storeFlavorId,
            @RequestBody UpdateStoreFlavorRestockRequest request
    ) {


        service.updateRestockSetting(
                storeFlavorId,
                request
        );


        return ResponseEntity.ok().build();

    }

}