package com.kiosk.branch.status.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.status.dto.BranchRestockRequestDTO;
import com.kiosk.branch.status.reopsitory.BranchRestockMapper;
import com.kiosk.branch.status.reopsitory.BranchRestockRequestMapper;
import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.RestockStatus;
import com.kiosk.headquarter.repository.HeadquarterAdminMapper;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class BranchRestockService {


    private final BranchRestockMapper branchRestockMapper;

    private final BranchRestockRequestMapper branchRestockRequestMapper;

    private final HeadquarterAdminMapper headquarterAdminMapper;



    /*
     * 발주 요청 등록
     */
    public String requestRestock(
            BranchRestockRequestDTO dto
    ) {


        if (
            dto.getRequestQuantity() == null ||
            dto.getRequestQuantity() <= 0
        ) {

            throw new IllegalArgumentException(
                    "발주 수량은 1개 이상이어야 합니다."
            );
        }
        
        /*
         * 상품과 맛 동시 선택 방지
         */
        if (
            dto.getStoreInventoryId() != null &&
            dto.getStoreFlavorId() != null
        ) {

            throw new IllegalArgumentException(
                    "상품과 맛 중 하나만 선택해야 합니다."
            );
        }



        RestockRequest restockRequest;



        /*
         * 상품 발주
         */
        if (
            dto.getStoreInventoryId() != null
        ) {


            StoreInventory inventory =
                    branchRestockMapper
                    .findStoreInventoryById(
                            dto.getStoreInventoryId()
                    );


            if(inventory == null) {

                throw new IllegalArgumentException(
                        "상품 재고 정보가 없습니다."
                );
            }



            restockRequest =
                    RestockRequest.builder()

                    .storeInventory(
                            inventory
                    )

                    .requestQuantity(
                            dto.getRequestQuantity()
                    )

                    .status(
                            RestockStatus.WAITING
                    )

                    .build();

        }



        /*
         * 맛 발주
         */
        else if (
            dto.getStoreFlavorId() != null
        ) {


            StoreFlavor flavor =
                    branchRestockMapper
                    .findStoreFlavorById(
                            dto.getStoreFlavorId()
                    );


            if(flavor == null) {

                throw new IllegalArgumentException(
                        "맛 재고 정보가 없습니다."
                );
            }



            restockRequest =
                    RestockRequest.builder()

                    .storeFlavor(
                            flavor
                    )

                    .requestQuantity(
                            dto.getRequestQuantity()
                    )

                    .status(
                            RestockStatus.WAITING
                    )

                    .build();

        }



        else {

            throw new IllegalArgumentException(
                    "발주 대상이 없습니다."
            );
        }



        /*
         * 현재 관리자 1번 고정
         */
        HeadquarterAdmin admin =
                headquarterAdminMapper
                .findById(1)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "기본 관리자가 없습니다."
                        )
                );



        /*
         * 관리자 지정
         */
        restockRequest.assignAdmin(admin);



        /*
         * 저장
         */
        branchRestockRequestMapper
                .insert(
                    restockRequest
                );



        return "발주 요청 완료";
    }

}