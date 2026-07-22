package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.headquarter.dto.storeFlavor.HeadStoreFlavorInventoryResponse;
import com.kiosk.headquarter.dto.storeFlavor.UpdateStoreFlavorRestockRequest;
import com.kiosk.headquarter.repository.StoreFlavorRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadStoreFlavorInventoryService {


    private final StoreFlavorRepository storeFlavorRepository;



    /*
     * 전체 지점 맛 재고 조회
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