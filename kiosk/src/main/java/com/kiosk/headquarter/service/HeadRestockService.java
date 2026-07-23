package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Delivery;
import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.DeliveryStatus;
import com.kiosk.entity.enums.RestockStatus;
import com.kiosk.headquarter.dto.restock.HeadRestockDetailResponseDTO;
import com.kiosk.headquarter.dto.restock.HeadRestockListResponseDTO;
import com.kiosk.headquarter.dto.restock.HeadRestockProcessRequestDTO;
import com.kiosk.headquarter.repository.DeliveryRepository;
import com.kiosk.headquarter.repository.HeadRestockRequestMapper;
import com.kiosk.headquarter.repository.HeadquarterAdminMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadRestockService {


    private final HeadRestockRequestMapper headRestockRequestMapper;
    private final HeadquarterAdminMapper headquarterAdminMapper;
    private final com.kiosk.headquarter.repository.AdminActionLogRepository adminActionLogRepository;
    private final DeliveryRepository deliveryRepository;


    // 전체 발주 목록
    public List<HeadRestockListResponseDTO> getRestockList() {

        return headRestockRequestMapper
                .findAllByOrderByIdDesc()
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }



    // 대기 발주 목록
    public List<HeadRestockListResponseDTO> getWaitingRestockList() {

        return headRestockRequestMapper
                .findByStatusOrderByIdDesc(RestockStatus.WAITING)
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }



    // 상세 조회
    public HeadRestockDetailResponseDTO getRestockDetail(Integer requestId) {

        RestockRequest restockRequest =
                headRestockRequestMapper.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 발주 요청입니다."
                        )
                );

        return toDetailResponseDTO(restockRequest);
    }



    // 승인
    @Transactional
    public String approveRestock(
            Integer requestId,
            HeadRestockProcessRequestDTO requestDTO
    ) {

        RestockRequest restockRequest =
                getRestock(requestId);

        HeadquarterAdmin admin =
                getActiveAdmin(requestDTO.getAdminId());


        restockRequest.approve(admin);


        return "발주 요청 승인 성공";
    }



    // 배송
    @Transactional
    public String startShipping(
            Integer requestId,
            HeadRestockProcessRequestDTO requestDTO
    ) {

        RestockRequest restockRequest =
                getRestock(requestId);


        HeadquarterAdmin admin =
                getActiveAdmin(requestDTO.getAdminId());


        restockRequest.startShipping(admin);



        if(!deliveryRepository.existsByRestockRequestId(requestId)) {


            Delivery delivery =
                    Delivery.builder()
                    .restockRequest(restockRequest)
                    .status(DeliveryStatus.READY)
                    .build();


            deliveryRepository.save(delivery);
        }



        return "발주 요청 배송 처리 성공";
    }



/*
    // 완료 및 재고 증가
    @Transactional
    public String completeRestock(
            Integer requestId,
            HeadRestockProcessRequestDTO requestDTO
    ) {


        RestockRequest restockRequest =
                getRestock(requestId);



        HeadquarterAdmin admin =
                getActiveAdmin(requestDTO.getAdminId());



        // SHIPPING -> COMPLETED
        restockRequest.complete(admin);



        /*
         * 제품 재고 입고
         */
    /*
        if (restockRequest.getStoreInventory() != null) {


            StoreInventory inventory =
                    restockRequest.getStoreInventory();


            inventory.increaseStock(
                    restockRequest.getRequestQuantity()
            );

        }



        /*
         * 맛 재고 입고
         */
    
    /*
        else if (restockRequest.getStoreFlavor() != null) {


            StoreFlavor flavor =
                    restockRequest.getStoreFlavor();


            flavor.increaseStock(
                    restockRequest.getRequestQuantity()
            );

        }


        else {

            throw new IllegalStateException(
                    "입고 대상 재고 정보가 없습니다."
            );
        }



        return "발주 완료 및 재고 입고 처리 성공";
    }


*/

    private RestockRequest getRestock(Integer requestId) {

        return headRestockRequestMapper.findById(requestId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 발주 요청입니다."
                        )
                );
    }





    private HeadquarterAdmin getActiveAdmin(Integer adminId) {


        if(adminId == null) {
            throw new IllegalArgumentException(
                    "처리 관리자 adminId가 필요합니다."
            );
        }


        HeadquarterAdmin admin =
                headquarterAdminMapper.findById(adminId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 본사 관리자입니다."
                        )
                );


        if(!admin.isActiveAdmin()) {

            throw new IllegalArgumentException(
                    "비활성화된 본사 관리자는 처리할 수 없습니다."
            );
        }


        return admin;
    }







    // 반려
    @Transactional
    public String rejectRestock(
            Integer requestId,
            HeadRestockProcessRequestDTO requestDTO
    ) {
        RestockRequest restockRequest = getRestock(requestId);
        HeadquarterAdmin admin = getActiveAdmin(requestDTO.getAdminId());

        restockRequest.reject(admin);

        // 반려 작업 로그 기록
        com.kiosk.entity.AdminActionLog log = com.kiosk.entity.AdminActionLog.builder()
                .administrator(admin.getName())
                .action("재고 신청 반려 (ID: " + requestId + ")")
                .type("RESTOCK_REJECT")
                .build();
        adminActionLogRepository.save(log);

        return "발주 요청 반려 성공";
    }

    private HeadRestockListResponseDTO toListResponseDTO(
            RestockRequest restockRequest
    ) {

        Integer adminId = null;
        String adminName = null;

        if (restockRequest.getStatus() == RestockStatus.CANCELED) {
            adminName = "지점 취소";
        } else if (restockRequest.getAdmin() != null) {
            adminId = restockRequest.getAdmin().getId();
            adminName = restockRequest.getAdmin().getName();
        }

        String itemName = "";
        String unit = "";
        String storeName = "";

        if (restockRequest.getStoreInventory() != null) {
            itemName = restockRequest.getStoreInventory().getItem().getProduct().getProductName();
            unit = restockRequest.getStoreInventory().getItem().getUnit();
            storeName = restockRequest.getStoreInventory().getStore().getStoreName();
        } else if (restockRequest.getStoreFlavor() != null) {
            itemName = restockRequest.getStoreFlavor().getFlavor().getFlavorName();
            unit = "EA";
            storeName = restockRequest.getStoreFlavor().getStore().getStoreName();
        }

        return HeadRestockListResponseDTO.builder()
                .requestId(
                        restockRequest.getId()
                )
                .itemName(itemName)
                .storeName(storeName)
                .unit(unit)


                .storeInventoryId(
                        restockRequest.getStoreInventoryId()
                )


                .storeFlavorId(
                        restockRequest.getStoreFlavorId()
                )


                .requestQuantity(
                        restockRequest.getRequestQuantity()
                )


                .status(
                        restockRequest.getStatus()
                )


                .adminId(adminId)

                .adminName(adminName)

                .requestedAt(
                        restockRequest.getRequestedAt()
                )

                .build();
    }







    private HeadRestockDetailResponseDTO toDetailResponseDTO(
            RestockRequest restockRequest
    ) {


        Integer adminId = null;
        String adminName = null;


        if(restockRequest.getAdmin()!=null) {

            adminId =
                    restockRequest.getAdmin().getId();

            adminName =
                    restockRequest.getAdmin().getName();
        }



        Integer unitPrice = 0;
        String name = "";
        String unit = "";



        /*
         * 제품 재고
         */
        if(restockRequest.getStoreInventory()!=null) {


            unitPrice =
                    restockRequest
                    .getStoreInventory()
                    .getItem()
                    .getUnitPrice();


            name =
                    restockRequest
                    .getStoreInventory()
                    .getItem()
                    .getProduct()
                    .getProductName();


            unit =
                    restockRequest
                    .getStoreInventory()
                    .getItem()
                    .getUnit();

        }



        /*
         * 맛 재고
         */
        else if(restockRequest.getStoreFlavor()!=null) {


            name =
                    restockRequest
                    .getStoreFlavor()
                    .getFlavor()
                    .getFlavorName();


            unit = "EA";

        }



        Integer quantity =
                restockRequest.getRequestQuantity();



        return HeadRestockDetailResponseDTO.builder()

                .requestId(
                        restockRequest.getId()
                )


                .storeInventoryId(
                        restockRequest.getStoreInventoryId()
                )


                .storeFlavorId(
                        restockRequest.getStoreFlavorId()
                )


                .itemName(name)

                .unit(unit)

                .unitPrice(unitPrice)

                .requestQuantity(quantity)

                .totalPrice(
                        unitPrice * quantity
                )

                .status(
                        restockRequest.getStatus()
                )

                .adminId(adminId)

                .adminName(adminName)

                .requestedAt(
                        restockRequest.getRequestedAt()
                )

                .build();
    }

}