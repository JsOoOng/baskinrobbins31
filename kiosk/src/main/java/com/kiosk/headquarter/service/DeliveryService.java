package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Delivery;
import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.DeliveryStatus;
import com.kiosk.headquarter.dto.deliverie.HeadDeliveryResponseDTO;
import com.kiosk.headquarter.repository.DeliveryRepository;
import com.kiosk.headquarter.repository.HeadquarterAdminRepository;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] DeliveryService
 *
 * <p>역할: 본사 관리의 배송 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> DeliveryRepository, HeadquarterAdminRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    private final HeadquarterAdminRepository headquarterAdminRepository;


    /**
     * 로그인 관리자 조회
     */
    private HeadquarterAdmin getLoginAdmin(
            Authentication authentication
    ){

        Integer adminId =
                (Integer) authentication.getPrincipal();


        return headquarterAdminRepository.findById(adminId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "관리자 정보를 찾을 수 없습니다."
                        )
                );
    }



    /**
     * 배송 목록 조회
     */
    @Transactional(readOnly = true)
    /**
     * [메서드 흐름] getDeliveries
     * Controller 또는 상위 서비스에서 호출되어 DeliveryRepository, HeadquarterAdminRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadDeliveryResponseDTO> getDeliveries(){


        return deliveryRepository.findAllDelivery()
                .stream()
                .map(this::toResponseDTO)
                .toList();

    }



    private HeadDeliveryResponseDTO toResponseDTO(
            Delivery delivery
    ){

        RestockRequest request =
                delivery.getRestockRequest();


        String storeName = null;
        String itemName = null;
        String unit = null;


        /*
         * 제품 재고 요청
         */
        if(request.getStoreInventory()!=null){

            StoreInventory inventory =
                    request.getStoreInventory();


            storeName =
                    inventory.getStore()
                            .getStoreName();


            itemName =
                    inventory.getItem()
                            .getProduct()
                            .getProductName();


            unit =
                    inventory.getItem()
                            .getUnit();

        }


        /*
         * 맛 재고 요청
         */
        else if(request.getStoreFlavor()!=null){

            StoreFlavor flavor =
                    request.getStoreFlavor();


            storeName =
                    flavor.getStore()
                          .getStoreName();


            itemName =
                    flavor.getFlavor()
                          .getFlavorName();


            unit = "EA";

        }



        return HeadDeliveryResponseDTO.builder()

                .deliveryId(
                        delivery.getId()
                )

                .restockRequestId(
                        request.getId()
                )

                .storeName(
                        storeName
                )

                .itemName(
                        itemName
                )

                .unit(
                        unit
                )

                .requestQuantity(
                        request.getRequestQuantity()
                )

                .restockStatus(
                        request.getStatus()
                )

                .requestedAt(
                        request.getRequestedAt()
                )

                .deliveryStatus(
                        delivery.getStatus()
                )

                .build();

    }




    /**
     * 배송 상태 변경
     */
    /**
     * [메서드 흐름] changeDeliveryStatus
     * Controller 또는 상위 서비스에서 호출되어 DeliveryRepository, HeadquarterAdminRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String changeDeliveryStatus(
            Integer deliveryId,
            DeliveryStatus status,
            Authentication authentication
    ){


        Delivery delivery =
                deliveryRepository.findById(deliveryId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "배송 정보를 찾을 수 없습니다."
                        )
                );


        delivery.changeStatus(status);



        if(status == DeliveryStatus.COMPLETED){


            HeadquarterAdmin admin =
                    getLoginAdmin(authentication);



            RestockRequest request =
                    delivery.getRestockRequest();



            request.complete(admin);



            if(request.getStoreInventory()!=null){

                request.getStoreInventory()
                        .increaseStock(
                                request.getRequestQuantity()
                        );

            }
            else if(request.getStoreFlavor()!=null){

                request.getStoreFlavor()
                        .increaseStock(
                                request.getRequestQuantity()
                        );
            }

        }


        return "배송 상태 변경 완료";

    }

}