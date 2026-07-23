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

/**
 * [코드 흐름 안내] HeadInventoryController
 *
 * <p>역할: 본사 관리의 재고 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/inventory) -> HeadInventoryService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
    /**
     * [요청 흐름] GET /head/inventory
     * 프론트 요청을 받아 getInventory() 메서드가 입력을 받고 HeadInventoryService 호출 후 결과를 응답한다.
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
    /**
     * [요청 흐름] GET /head/inventory/{storeInventoryId}
     * 프론트 요청을 받아 getInventory() 메서드가 입력을 받고 HeadInventoryService 호출 후 결과를 응답한다.
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
    /**
     * [요청 흐름] PATCH /head/inventory
     * 프론트 요청을 받아 updateRestockSetting() 메서드가 입력을 받고 HeadInventoryService 호출 후 결과를 응답한다.
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