package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.headquarter.dto.storeFlavor.HeadStoreFlavorInventoryResponse;
import com.kiosk.headquarter.dto.storeFlavor.UpdateStoreFlavorRestockRequest;
import com.kiosk.headquarter.repository.StoreFlavorRepository;

import lombok.RequiredArgsConstructor;


/**
 * [코드 흐름 안내] HeadStoreFlavorInventoryService
 *
 * <p>역할: 본사 관리의 재고 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> StoreFlavorRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStoreFlavorInventoryService {


    private final StoreFlavorRepository storeFlavorRepository;



    /*
     * 전체 지점 맛 재고 조회
     */
    /**
     * [메서드 흐름] getStoreFlavorInventories
     * Controller 또는 상위 서비스에서 호출되어 StoreFlavorRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadStoreFlavorInventoryResponse> getStoreFlavorInventories() {


        return storeFlavorRepository
                .findAll()
                .stream()
                .map(
                    HeadStoreFlavorInventoryResponse::from
                )
                .toList();

    }



    /*
     * 특정 지점 맛 재고 조회
     */
    /**
     * [메서드 흐름] getStoreFlavorInventories
     * Controller 또는 상위 서비스에서 호출되어 StoreFlavorRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadStoreFlavorInventoryResponse> getStoreFlavorInventories(
            Integer storeId
    ) {


        return storeFlavorRepository
                .findByStore_Id(storeId)
                .stream()
                .map(
                    HeadStoreFlavorInventoryResponse::from
                )
                .toList();

    }



    /*
     * 자동 보충 설정 수정
     */
    @Transactional
    /**
     * [메서드 흐름] updateRestockSetting
     * Controller 또는 상위 서비스에서 호출되어 StoreFlavorRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void updateRestockSetting(
            Integer storeFlavorId,
            UpdateStoreFlavorRestockRequest request
    ) {


        StoreFlavor storeFlavor =
                storeFlavorRepository.findById(
                        storeFlavorId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "맛 재고 정보를 찾을 수 없습니다."
                        )
                );


        storeFlavor.updateAutoRestockSetting(

                request.getMinStock(),

                request.getTargetStock(),

                request.getAutoRestockEnabled(),

                request.getRestockMode()

        );

    }

}