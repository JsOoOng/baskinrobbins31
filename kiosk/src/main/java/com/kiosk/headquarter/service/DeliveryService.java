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
import com.kiosk.entity.enums.RestockStatus;
import com.kiosk.headquarter.dto.deliverie.HeadDeliveryResponseDTO;
import com.kiosk.headquarter.repository.DeliveryRepository;
import com.kiosk.headquarter.repository.HeadquarterAdminRepository;
import com.kiosk.common.websocket.BranchRestockStatusSocketPublisher;
import com.kiosk.branch.notification.service.BranchNotificationService;

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
    private final AdminLogService adminLogService;
    private final HeadRestockService headRestockService;
    private final BranchRestockStatusSocketPublisher branchRestockStatusSocketPublisher;
    private final BranchNotificationService branchNotificationService;


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
    /**
     * [메서드 흐름] getDeliveries
     * Controller 또는 상위 서비스에서 호출되어 DeliveryRepository, HeadquarterAdminRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadDeliveryResponseDTO> getDeliveries(){

        /*
         * 수정 전에 승인되어 배송 행이 없는 신청도 배송 관리에 즉시 나타나도록 복구합니다.
         * 새 승인 건은 HeadRestockService에서 바로 생성되므로 보통 이 목록은 비어 있습니다.
         */
        deliveryRepository.findRequestsWithoutDelivery(RestockStatus.APPROVED)
                .forEach(request -> {
                    deliveryRepository.save(
                            Delivery.builder()
                                    .restockRequest(request)
                                    .status(DeliveryStatus.READY)
                                    .build()
                    );

                    Integer storeId = request.getStoreInventory() != null
                            ? request.getStoreInventory().getStore().getId()
                            : request.getStoreFlavor().getStore().getId();
                    String itemName = request.getStoreInventory() != null
                            ? request.getStoreInventory().getItem().getItemName()
                            : request.getStoreFlavor().getFlavor().getFlavorName();

                    /*
                     * 과거 승인 건이 알림도 놓친 경우를 함께 복구합니다.
                     * createOnce가 신청 번호 기준 중복을 막으므로 이미 받은 지점에는 다시 쌓이지 않습니다.
                     */
                    branchNotificationService.createOnce(
                            storeId,
                            "RESTOCK_REQUEST_APPROVED",
                            String.valueOf(request.getId()),
                            "재고 신청이 승인되었습니다.",
                            itemName + " " + request.getRequestQuantity()
                                    + "개 신청이 승인되었습니다."
                    );
                });

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

                .cancelReason(
                        delivery.getCancelReason()
                )
                .notificationSent(delivery.getNotificationSent())
                .managementCompleted(delivery.getManagementCompleted())

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


        RestockRequest request = delivery.getRestockRequest();

        /*
         * 쉬운주석: 배송 화면과 재고 신청 화면은 서로 다른 상태값을 사용한다.
         * 배송 시작 때 신청도 SHIPPING으로 맞춰야 완료 시 재고 입고 규칙을 통과한다.
         */
        if (status == DeliveryStatus.STARTED
                && request.getStatus() == RestockStatus.APPROVED) {
            request.startShipping(getLoginAdmin(authentication));
        }

        delivery.changeStatus(status);

        if(status == DeliveryStatus.COMPLETED){
            /*
             * 쉬운주석: 수정 전에 이미 배송 중이 된 기존 데이터는 신청 상태가 APPROVED일 수 있다.
             * 이런 신청도 먼저 SHIPPING으로 맞춘 뒤 한 번만 완료·입고 처리한다.
             */
            if (request.getStatus() == RestockStatus.APPROVED) {
                request.startShipping(getLoginAdmin(authentication));
            }

            headRestockService.completeAndReceive(
                    request,
                    getLoginAdmin(authentication)
            );

        }


        adminLogService.logAction("배송",
                "배송 상태 변경 (ID: " + deliveryId + ", 상태: " + status + ")");
        return "배송 상태 변경 완료";

    }

    /**
     * 배송 관리 화면의 '지점 알림' 버튼이 호출합니다.
     * 배송 완료 상태를 확인한 뒤 해당 지점에 완료 안내를 한 번만 전송합니다.
     */
    public String notifyDeliveryCompleted(Integer deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));

        if (delivery.getStatus() != DeliveryStatus.COMPLETED) {
            throw new IllegalStateException("배송 완료 상태에서만 지점 알림을 보낼 수 있습니다.");
        }

        RestockRequest request = delivery.getRestockRequest();
        Integer storeId = request.getStoreInventory() != null
                ? request.getStoreInventory().getStore().getId()
                : request.getStoreFlavor().getStore().getId();
        String itemName = request.getStoreInventory() != null
                ? request.getStoreInventory().getItem().getItemName()
                : request.getStoreFlavor().getFlavor().getFlavorName();

        String message = itemName + " " + request.getRequestQuantity()
                + "개 배송이 완료되어 재고에 반영되었습니다.";

        if (Boolean.TRUE.equals(delivery.getNotificationSent())) {
            branchNotificationService.resend(
                    storeId,
                    "DELIVERY_COMPLETED",
                    String.valueOf(deliveryId),
                    "배송 및 입고 완료",
                    message
            );
        } else {
            branchNotificationService.createOnce(
                    storeId,
                    "DELIVERY_COMPLETED",
                    String.valueOf(deliveryId),
                    "배송 및 입고 완료",
                    message
            );
            delivery.markNotificationSent();
        }

        return "지점 배송 완료 알림 전송 완료";
    }

    /** 알림 전송을 확인한 관리자가 해당 배송 업무를 최종 완료합니다. */
    public String completeDeliveryManagement(Integer deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다."));
        delivery.completeManagement();
        adminLogService.logAction("배송", "배송 관리 완료 (ID: " + deliveryId + ")");
        return "배송 관리 완료";
    }

    /**
     * 쉬운주석: 배송 취소, 재고 신청 반려, 지점 알림, 작업 로그를 한 번에 처리한다.
     * 메서드가 실패하면 트랜잭션이 롤백되어 일부 상태만 바뀌는 일을 막는다.
     */
    public String cancelDelivery(
            Integer deliveryId,
            String reason,
            Authentication authentication
    ) {
        String cancelReason = reason == null ? "" : reason.trim();
        if (cancelReason.isBlank()) {
            throw new IllegalArgumentException("배송 취소 사유를 입력해주세요.");
        }
        if (cancelReason.length() > 500) {
            throw new IllegalArgumentException("배송 취소 사유는 500자 이하로 입력해주세요.");
        }

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "배송 정보를 찾을 수 없습니다."
                ));
        HeadquarterAdmin admin = getLoginAdmin(authentication);
        RestockRequest request = delivery.getRestockRequest();

        delivery.cancel(cancelReason);
        request.rejectDelivery(admin, cancelReason);

        /*
         * 쉬운주석: DB의 취소·반려 상태가 정상 저장된 뒤 해당 지점 화면에
         * '재고 신청이 반려되었습니다.' 실시간 알림을 보낸다.
         */
        branchRestockStatusSocketPublisher.publishAfterCommit(request);
        adminLogService.logAction(
                "배송",
                "배송 취소 (ID: " + deliveryId + ", 사유: " + cancelReason + ")"
        );

        return "배송 취소 및 재고 신청 반려 완료";
    }

}
