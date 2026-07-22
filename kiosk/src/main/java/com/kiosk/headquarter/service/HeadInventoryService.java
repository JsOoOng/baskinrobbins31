package com.kiosk.headquarter.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.StoreInventory;
import com.kiosk.headquarter.dto.inventory.HeadInventoryResponse;
import com.kiosk.headquarter.dto.inventory.HeadInventoryUpdateRequest;
import com.kiosk.headquarter.inventory.AutoRestockService;
import com.kiosk.headquarter.repository.HeadStoreInventoryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadInventoryService {

    private final HeadStoreInventoryMapper
            headStoreInventoryMapper;

    private final AutoRestockService
            autoRestockService;

    /*
     * 전체 지점 재고 조회
     *
     * 지점 번호와 재고 품목 번호 순서로 정렬합니다.
     */
    public List<HeadInventoryResponse>
	    getInventories() {
	
	return headStoreInventoryMapper
	        .findAll()
	        .stream()
	        .sorted(
	                Comparator
	                        .comparing(
	                                (StoreInventory inventory) ->
	                                        inventory
	                                                .getStore()
	                                                .getId()
	                        )
	                        .thenComparing(
	                                inventory ->
	                                        inventory
	                                                .getItem()
	                                                .getId()
	                        )
	        )
	        .map(
	                HeadInventoryResponse::from
	        )
	        .toList();
	}

    /*
     * 재고 상세 조회
     */
    public HeadInventoryResponse getInventory(
            Integer storeInventoryId
    ) {

        StoreInventory inventory =
                findInventory(
                        storeInventoryId
                );

        return HeadInventoryResponse.from(
                inventory
        );
    }

    /*
     * 자동 재고 보충 설정 수정
     */
    @Transactional
    public HeadInventoryResponse
            updateRestockSetting(
                    Integer storeInventoryId,
                    HeadInventoryUpdateRequest request
            ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "재고 설정 요청이 없습니다."
            );
        }

        validateRestockSetting(request);

        StoreInventory inventory =
                findInventory(
                        storeInventoryId
                );

        /*
         * 1. 자동 보충 설정 변경
         */
        inventory.updateAutoRestockSetting(
                request.getAutoRestockEnabled(),
                request.getRestockMode(),
                request.getMinStock(),
                request.getTargetStock()
        );

        /*
         * 2. THRESHOLD 또는 BOTH로 설정했고,
         * 현재 재고가 이미 최소 재고 이하라면
         * 설정 저장 즉시 목표 재고까지 보충합니다.
         */
        Integer restockedQuantity =
                autoRestockService
                        .processThresholdRestock(
                                inventory
                        );

        if (restockedQuantity > 0) {
            System.out.println(
                    "재고 설정 저장 후 임계 자동 보충: "
                            + "storeInventoryId="
                            + inventory.getId()
                            + ", restockedQuantity="
                            + restockedQuantity
                            + ", currentStock="
                            + inventory.getCurrentStock()
            );
        }

        return HeadInventoryResponse.from(
                inventory
        );
    }

    /*
     * 재고 조회 공통 처리
     */
    private StoreInventory findInventory(
            Integer storeInventoryId
    ) {

        if (storeInventoryId == null) {
            throw new IllegalArgumentException(
                    "재고 번호가 없습니다."
            );
        }

        return headStoreInventoryMapper
                .findById(storeInventoryId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 재고입니다."
                        )
                );
    }

    /*
     * 자동 재고 보충 설정 검증
     */
    private void validateRestockSetting(
            HeadInventoryUpdateRequest request
    ) {

        if (
                request.getAutoRestockEnabled()
                        == null
        ) {
            throw new IllegalArgumentException(
                    "자동 재고 보충 여부를 선택해주세요."
            );
        }

        if (
                request.getRestockMode()
                        == null
        ) {
            throw new IllegalArgumentException(
                    "자동 재고 보충 방식을 선택해주세요."
            );
        }

        if (
                request.getMinStock() == null ||
                request.getMinStock() < 0
        ) {
            throw new IllegalArgumentException(
                    "최소 재고는 0 이상이어야 합니다."
            );
        }

        if (
                request.getTargetStock() == null ||
                request.getTargetStock() < 1
        ) {
            throw new IllegalArgumentException(
                    "목표 재고는 1 이상이어야 합니다."
            );
        }

        if (
                request.getTargetStock()
                        <= request.getMinStock()
        ) {
            throw new IllegalArgumentException(
                    "목표 재고는 최소 재고보다 커야 합니다."
            );
        }
    }
}