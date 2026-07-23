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