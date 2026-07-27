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
import com.kiosk.branch.notification.service.BranchNotificationService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadRestockService
 *
 * <p>역할: 본사 관리의 재입고·발주 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadRestockRequestMapper, HeadquarterAdminMapper, AdminActionLogRepository, BranchRestockStatusSocketPublisher 등 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadRestockService {


	private final HeadRestockRequestMapper headRestockRequestMapper;
	private final HeadquarterAdminMapper headquarterAdminMapper;
	private final AdminLogService adminLogService;

    private final DeliveryRepository deliveryRepository;
    private final BranchNotificationService branchNotificationService;


    // 전체 발주 목록
    /**
     * [메서드 흐름] getRestockList
     * Controller 또는 상위 서비스에서 호출되어 HeadRestockRequestMapper, HeadquarterAdminMapper, AdminActionLogRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadRestockListResponseDTO> getRestockList() {

        return headRestockRequestMapper
                .findAllByOrderByIdDesc()
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }



    // 대기 발주 목록
    /**
     * [메서드 흐름] getWaitingRestockList
     * Controller 또는 상위 서비스에서 호출되어 HeadRestockRequestMapper, HeadquarterAdminMapper, AdminActionLogRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadRestockListResponseDTO> getWaitingRestockList() {

        return headRestockRequestMapper
                .findByStatusOrderByIdDesc(RestockStatus.WAITING)
                .stream()
                .map(this::toListResponseDTO)
                .toList();
    }



    // 상세 조회
    /**
     * [메서드 흐름] getRestockDetail
     * Controller 또는 상위 서비스에서 호출되어 HeadRestockRequestMapper, HeadquarterAdminMapper, AdminActionLogRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] approveRestock
     * Controller 또는 상위 서비스에서 호출되어 HeadRestockRequestMapper, HeadquarterAdminMapper, AdminActionLogRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String approveRestock(
            Integer requestId,
            HeadRestockProcessRequestDTO requestDTO
    ) {

        RestockRequest restockRequest =
                getRestock(
                        requestId
                );

        HeadquarterAdmin admin =
                getActiveAdmin(
                        requestDTO.getAdminId()
                );

        /*
         * WAITING → APPROVED
         */
        restockRequest.approve(
                admin
        );

        /*
         * 승인된 신청은 배송 관리에서 바로 보일 수 있도록
         * READY 상태의 배송 정보를 같은 트랜잭션에서 생성합니다.
         */
        if (!deliveryRepository.existsByRestockRequestId(requestId)) {
            deliveryRepository.save(
                    Delivery.builder()
                            .restockRequest(restockRequest)
                            .status(DeliveryStatus.READY)
                            .build()
            );
        }

        /*
         * 승인 알림을 DB에 보관하고 실시간으로도 전송합니다.
         * 지점이 승인 순간 오프라인이어도 다음 접속 때 읽지 않은 알림을 받을 수 있습니다.
         */
        branchNotificationService.createOnce(
                getStoreId(restockRequest),
                "RESTOCK_REQUEST_APPROVED",
                String.valueOf(requestId),
                "재고 신청이 승인되었습니다.",
                getItemName(restockRequest) + " "
                        + restockRequest.getRequestQuantity()
                        + "개 신청이 승인되었습니다."
        );

        adminLogService.logAction("재고 신청", "재고 신청 승인 (ID: " + requestId + ")");
        return "발주 요청 승인 성공";
    }



    // 배송
    @Transactional
    /**
     * [메서드 흐름] startShipping
     * Controller 또는 상위 서비스에서 호출되어 HeadRestockRequestMapper, HeadquarterAdminMapper, AdminActionLogRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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



        adminLogService.logAction("재고 신청", "재고 신청 배송 시작 (ID: " + requestId + ")");
        return "발주 요청 배송 처리 성공";
    }



    // 배송 완료 및 실제 재고 입고
    @Transactional
    public String completeRestock(
            Integer requestId,
            HeadRestockProcessRequestDTO requestDTO
    ) {
        RestockRequest restockRequest =
                getRestock(requestId);
        HeadquarterAdmin admin =
                getActiveAdmin(requestDTO.getAdminId());
        Delivery delivery = deliveryRepository
                .findByRestockRequestId(requestId)
                .orElseThrow(() -> new IllegalStateException(
                        "연결된 배송 정보가 없습니다."
                ));

        delivery.changeStatus(DeliveryStatus.COMPLETED);
        completeAndReceive(restockRequest, admin);
        adminLogService.logAction(
                "재고 신청",
                "재고 신청 배송 완료 (ID: " + requestId + ")"
        );
        return "발주 완료 및 재고 입고 처리 성공";
    }

    /**
     * 배송 관리와 재고 신청 관리가 공유하는 최종 완료 처리다.
     * 신청 상태와 상품·맛 재고를 같은 트랜잭션에서 함께 변경한다.
     */
    public void completeAndReceive(
            RestockRequest restockRequest,
            HeadquarterAdmin admin
    ) {
        restockRequest.complete(admin);

        if (restockRequest.getStoreInventory() != null) {
            restockRequest.getStoreInventory().increaseStock(
                    restockRequest.getRequestQuantity()
            );
        } else if (restockRequest.getStoreFlavor() != null) {
            restockRequest.getStoreFlavor().increaseStock(
                    restockRequest.getRequestQuantity()
            );
        } else {
            throw new IllegalStateException("입고 대상 재고 정보가 없습니다.");
        }
    }

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
    /**
     * [메서드 흐름] rejectRestock
     * Controller 또는 상위 서비스에서 호출되어 HeadRestockRequestMapper, HeadquarterAdminMapper, AdminActionLogRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String rejectRestock(
            Integer requestId,
            HeadRestockProcessRequestDTO requestDTO
    ) {
        String rejectionReason = requestDTO.getRejectionReason() == null
                ? ""
                : requestDTO.getRejectionReason().trim();
        if (rejectionReason.isBlank()) {
            throw new IllegalArgumentException("반려 사유를 입력해주세요.");
        }
        if (rejectionReason.length() > 500) {
            throw new IllegalArgumentException("반려 사유는 500자 이하로 입력해주세요.");
        }

        RestockRequest restockRequest =
                getRestock(
                        requestId
                );

        HeadquarterAdmin admin =
                getActiveAdmin(
                        requestDTO.getAdminId()
                );

        /*
         * WAITING → REJECTED
         */
        restockRequest.reject(
                admin,
                rejectionReason
        );

        adminLogService.logAction(
                "재고 신청",
                "재고 신청 반려 (ID: " + requestId + ", 사유: " + rejectionReason + ")"
        );

        /*
         * 반려 결과도 저장형 지점 알림으로 전송해 접속 여부와 관계없이 확인할 수 있게 합니다.
         */
        branchNotificationService.createOnce(
                getStoreId(restockRequest),
                "RESTOCK_REQUEST_REJECTED",
                String.valueOf(requestId),
                "재고 신청이 반려되었습니다.",
                getItemName(restockRequest) + " 신청이 반려되었습니다. 사유: "
                        + rejectionReason
        );

        return "발주 요청 반려 성공";
    }

    /* 일반 상품 신청과 맛 신청에서 알림을 받을 지점 번호를 공통으로 찾습니다. */
    private Integer getStoreId(RestockRequest request) {
        if (request.getStoreInventory() != null) {
            return request.getStoreInventory().getStore().getId();
        }
        if (request.getStoreFlavor() != null) {
            return request.getStoreFlavor().getStore().getId();
        }
        throw new IllegalStateException("재고 신청의 지점 정보가 없습니다.");
    }

    /* 지점 알림 문구에 표시할 상품 또는 맛 이름을 공통으로 찾습니다. */
    private String getItemName(RestockRequest request) {
        if (request.getStoreInventory() != null) {
            return request.getStoreInventory().getItem().getItemName();
        }
        if (request.getStoreFlavor() != null) {
            return request.getStoreFlavor().getFlavor().getFlavorName();
        }
        return "재고 품목";
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
        	itemName =
        	        restockRequest
        	                .getStoreInventory()
        	                .getItem()
        	                .getProduct()
        	                .getProductName();
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
                            .getItemName();

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
