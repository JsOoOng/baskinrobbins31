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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * [코드 흐름 안내] HeadStoreFlavorInventoryController
 *
 * <p>역할: 본사 관리의 재고 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/flavor-inventory) -> HeadStoreFlavorInventoryService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/flavor-inventory")
@RequiredArgsConstructor
public class HeadStoreFlavorInventoryController {


    private final HeadStoreFlavorInventoryService service;



    /*
     * 전체 지점 맛 재고 조회
     */
    /**
     * [요청 흐름] GET /head/flavor-inventory
     * 프론트 요청을 받아 getInventories() 메서드가 입력을 받고 HeadStoreFlavorInventoryService 호출 후 결과를 응답한다.
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
    /**
     * [요청 흐름] GET /head/flavor-inventory/store/{storeId}
     * 프론트 요청을 받아 getStoreInventories() 메서드가 입력을 받고 HeadStoreFlavorInventoryService 호출 후 결과를 응답한다.
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
    /**
     * [요청 흐름] PATCH /head/flavor-inventory/{storeFlavorId}
     * 프론트 요청을 받아 updateRestockSetting() 메서드가 입력을 받고 HeadStoreFlavorInventoryService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/{storeFlavorId}")
    public ResponseEntity<HeadStoreFlavorInventoryResponse> updateRestockSetting(
            @PathVariable Integer storeFlavorId,
            @RequestBody UpdateStoreFlavorRestockRequest request
    ) {

        return ResponseEntity.ok(
                service.updateRestockSetting(
                        storeFlavorId,
                        request
                )
        );

    }

    /*
     * 아이스크림 맛 재고 통 입고 HTTP 진입점
     *
     * HeadStoreFlavor.vue의 입고 모달
     * → PATCH /head/flavor-inventory/{storeFlavorId}/stock-in
     * → Service.stockIn()
     * → StoreFlavor.container 증가
     * → 최신 재고 DTO 반환 순서로 이동합니다.
     */
    @PatchMapping("/{storeFlavorId}/stock-in")
    public ResponseEntity<HeadStoreFlavorInventoryResponse>
            stockIn(
                    @PathVariable Integer storeFlavorId,
                    @Valid @RequestBody
                    StoreFlavorStockInRequest request
            ) {
        return ResponseEntity.ok(
                service.stockIn(
                        storeFlavorId,
                        request.getContainerQuantity()
                )
        );
    }

    /*
     * 입고 요청 JSON의 containerQuantity를 받는 전용 DTO입니다.
     * 상품 개수가 아닌 아이스크림 통 수이며 1통 이상만 허용합니다.
     */
    @Getter
    @NoArgsConstructor
    public static class StoreFlavorStockInRequest {
        @NotNull(message = "입고할 통 수를 입력해주세요.")
        @Min(value = 1, message = "입고할 통 수는 1 이상이어야 합니다.")
        private Integer containerQuantity;
    }

}
