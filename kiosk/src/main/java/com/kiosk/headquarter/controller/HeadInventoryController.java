package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.inventory.HeadInventoryResponse;
import com.kiosk.headquarter.dto.inventory.HeadInventoryUpdateRequest;
import com.kiosk.headquarter.service.HeadInventoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/head/inventory")
@RequiredArgsConstructor
public class HeadInventoryController {

    private final HeadInventoryService
            headInventoryService;

    /*
     * 전체 지점 재고 조회
     *
     * GET /head/inventory
     */
    @GetMapping
    public ResponseEntity<
            List<HeadInventoryResponse>
    > getInventories() {

        List<HeadInventoryResponse> inventories =
                headInventoryService
                        .getInventories();

        return ResponseEntity.ok(
                inventories
        );
    }

    /*
     * 재고 상세 조회
     *
     * GET /head/inventory/{storeInventoryId}
     */
    @GetMapping("/{storeInventoryId}")
    public ResponseEntity<HeadInventoryResponse>
            getInventory(
                    @PathVariable
                    Integer storeInventoryId
            ) {

        HeadInventoryResponse inventory =
                headInventoryService
                        .getInventory(
                                storeInventoryId
                        );

        return ResponseEntity.ok(
                inventory
        );
    }

    /*
     * 자동 재고 보충 설정 수정
     *
     * PATCH
     * /head/inventory/{storeInventoryId}/restock-setting
     */
    @PatchMapping(
            "/{storeInventoryId}/restock-setting"
    )
    public ResponseEntity<HeadInventoryResponse>
            updateRestockSetting(
                    @PathVariable
                    Integer storeInventoryId,

                    @Valid
                    @RequestBody
                    HeadInventoryUpdateRequest request
            ) {

        HeadInventoryResponse inventory =
                headInventoryService
                        .updateRestockSetting(
                                storeInventoryId,
                                request
                        );

        return ResponseEntity.ok(
                inventory
        );
    }
}