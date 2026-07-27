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

/**
 * [코드 흐름 안내] BranchInventoryShortageAlertController
 *
 * <p>역할: 지점 운영의 재고 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch/stores/{storeId}/inventory-shortage-alerts) -> InventoryShortageAlertService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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