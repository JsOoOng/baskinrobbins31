package com.kiosk.branch.inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.inventory.dto.BranchInventoryShortageConfirmResponse;
import com.kiosk.branch.inventory.dto.BranchInventoryShortageSummaryResponse;
import com.kiosk.headquarter.service.InventoryShortageAlertService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(
        "/branch/stores/{storeId}/inventory-shortage-alerts"
)
@RequiredArgsConstructor
public class BranchInventoryShortageAlertController {

    private final InventoryShortageAlertService
            inventoryShortageAlertService;

    /*
     * 지점의 미확인 재고 부족 알림 조회
     *
     * GET
     * /branch/stores/{storeId}
     * /inventory-shortage-alerts/unconfirmed
     */
    @GetMapping("/unconfirmed")
    public ResponseEntity<
            BranchInventoryShortageSummaryResponse
    > getUnconfirmedAlerts(
            @PathVariable
            Integer storeId
    ) {

        BranchInventoryShortageSummaryResponse response =
                inventoryShortageAlertService
                        .getPendingBranchAlertSummary(
                                storeId
                        );

        return ResponseEntity.ok(
                response
        );
    }

    /*
     * 지점의 미확인 부족 알림 일괄 확인
     *
     * PATCH
     * /branch/stores/{storeId}
     * /inventory-shortage-alerts/confirm
     */
    @PatchMapping("/confirm")
    public ResponseEntity<
            BranchInventoryShortageConfirmResponse
    > confirmAlerts(
            @PathVariable
            Integer storeId
    ) {

        BranchInventoryShortageConfirmResponse response =
                inventoryShortageAlertService
                        .confirmPendingBranchAlerts(
                                storeId
                        );

        return ResponseEntity.ok(
                response
        );
    }
}