package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.inventoryalert.HeadInventoryAlertSendRequest;
import com.kiosk.headquarter.dto.inventoryalert.HeadInventoryShortageSocketResponse;
import com.kiosk.headquarter.service.InventoryShortageAlertService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadInventoryShortageAlertController
 *
 * <p>역할: 본사 관리의 재고 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/inventory-shortage-alerts) -> InventoryShortageAlertService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping(
        "/head/inventory-shortage-alerts"
)
@RequiredArgsConstructor
public class HeadInventoryShortageAlertController {

    private final InventoryShortageAlertService
            inventoryShortageAlertService;

    /*
     * 본사 재고 현황용
     * 활성 부족 알람 목록 조회
     *
     * GET
     * /head/inventory-shortage-alerts
     */
    @GetMapping
    public ResponseEntity<
            List<HeadInventoryShortageSocketResponse>
    > getActiveAlerts() {

        List<HeadInventoryShortageSocketResponse>
                responses =
                inventoryShortageAlertService
                        .getAllActiveAlertResponses();

        return ResponseEntity.ok(
                responses
        );
    }

    /*
     * 본사 관리자가 해당 지점에
     * 부족 알람을 전송
     *
     * POST
     * /head/inventory-shortage-alerts/{alertId}/send
     */
    @PostMapping(
            "/{alertId}/send"
    )
    public ResponseEntity<
            HeadInventoryShortageSocketResponse
    > sendAlertToStore(
            @PathVariable
            Integer alertId,

            @RequestBody
            HeadInventoryAlertSendRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "알람 전송 요청 정보가 없습니다."
            );
        }

        HeadInventoryShortageSocketResponse response =
                inventoryShortageAlertService
                        .sendAlertToStore(
                                alertId,
                                request.getAdminId()
                        );

        return ResponseEntity.ok(
                response
        );
    }
}