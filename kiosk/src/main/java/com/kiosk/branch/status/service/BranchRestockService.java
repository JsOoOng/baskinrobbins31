package com.kiosk.branch.status.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.status.dto.BranchRestockListResponseDTO;
import com.kiosk.branch.status.dto.BranchRestockRequestDTO;
import com.kiosk.branch.status.reopsitory.BranchRestockMapper;
import com.kiosk.branch.status.reopsitory.BranchRestockRequestMapper;
import com.kiosk.common.websocket.RestockRequestSocketPublisher;
import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.NotificationCategory;
import com.kiosk.entity.enums.NotificationType;
import com.kiosk.entity.enums.RestockStatus;
import com.kiosk.headquarter.repository.HeadRestockRequestMapper;
import com.kiosk.headquarter.service.HeadNotificationService;
import com.kiosk.headquarter.service.InventoryShortageAlertService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] BranchRestockService
 *
 * <p>역할: 지점 운영의 재입고·발주 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> BranchRestockMapper, BranchRestockRequestMapper, HeadRestockRequestMapper, InventoryShortageAlertService 등 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BranchRestockService {

    private final BranchRestockMapper
            branchRestockMapper;

    private final BranchRestockRequestMapper
            branchRestockRequestMapper;

    private final HeadRestockRequestMapper
            headRestockRequestMapper;

    private final InventoryShortageAlertService
            inventoryShortageAlertService;

    private final HeadNotificationService
            headNotificationService;

    private final RestockRequestSocketPublisher
            restockRequestSocketPublisher;

    /*
     * 지점 재고 신청
     */
    /**
     * [메서드 흐름] requestRestock
     * Controller 또는 상위 서비스에서 호출되어 BranchRestockMapper, BranchRestockRequestMapper, HeadRestockRequestMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String requestRestock(
            BranchRestockRequestDTO dto
    ) {

        validateRequest(
                dto
        );

        RestockRequest restockRequest;

        Integer storeId;
        String storeName = "지점";
        String itemName;
        String requestTargetType;

        /*
         * 일반 재고 신청
         */
        if (
                dto.getStoreInventoryId()
                        != null
        ) {

            StoreInventory inventory =
                    branchRestockMapper
                            .findStoreInventoryById(
                                    dto.getStoreInventoryId()
                            );

            if (inventory == null) {
                throw new IllegalArgumentException(
                        "상품 재고 정보가 없습니다."
                );
            }

            validateStoreInventory(
                    inventory
            );

            restockRequest =
                    RestockRequest
                            .builder()
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

            storeId =
                    inventory.getStore()
                            .getId();

            storeName =
                    getStoreName(
                            inventory
                                    .getStore()
                                    .getStoreName()
                    );

            itemName =
                    getItemName(
                            inventory
                                    .getItem()
                                    .getItemName()
                    );

            requestTargetType =
                    "INVENTORY";

        } else {

            /*
             * 아이스크림 맛 재고 신청
             */
            StoreFlavor flavor =
                    branchRestockMapper
                            .findStoreFlavorById(
                                    dto.getStoreFlavorId()
                            );

            if (flavor == null) {
                throw new IllegalArgumentException(
                        "맛 재고 정보가 없습니다."
                );
            }

            validateStoreFlavor(
                    flavor
            );

            /*
             * 맛 알람과 alertId 연결은
             * 2차 맛 부족 알람 기능에서 구현합니다.
             */
            if (dto.getAlertId() != null) {
                throw new IllegalArgumentException(
                        "맛 재고 부족 알람 연결은 "
                        + "맛 부족 알람 기능에서 처리합니다."
                );
            }

            restockRequest =
                    RestockRequest
                            .builder()
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

            storeId =
                    flavor.getStore()
                            .getId();

            storeName =
                    getStoreName(
                            flavor.getStore()
                                    .getStoreName()
                    );

            itemName =
                    getItemName(
                            flavor.getFlavor()
                                    .getFlavorName()
                    );

            requestTargetType =
                    "FLAVOR";
        }

        /*
         * 지점 신청 시에는
         * 처리 본사 관리자를 지정하지 않습니다.
         *
         * admin_id = NULL
         */
        int insertedRows =
                branchRestockRequestMapper
                        .insert(
                                restockRequest
                        );

        if (insertedRows != 1) {
            throw new IllegalStateException(
                    "재고 신청 저장에 실패했습니다."
            );
        }

        Integer requestId =
                restockRequest.getId();

        if (
                requestId == null ||
                requestId <= 0
        ) {
            throw new IllegalStateException(
                    "생성된 재고 신청 번호를 확인할 수 없습니다."
            );
        }

        /*
         * 일반 재고 부족 알람에서 신청한 경우
         *
         * CONFIRMED → REQUESTED
         */
        if (
                "INVENTORY".equals(
                        requestTargetType
                )
                &&
                dto.getAlertId() != null
        ) {

            inventoryShortageAlertService
                    .markRequested(
                            dto.getAlertId(),
                            dto.getStoreInventoryId(),
                            requestId
                    );
        }

        /*
         * 본사 상단 알림 DB 저장
         */
        NotificationType notificationType =
                "FLAVOR".equals(
                        requestTargetType
                )
                        ? NotificationType
                                .FLAVOR_RESTOCK_REQUEST_CREATED
                        : NotificationType
                                .RESTOCK_REQUEST_CREATED;

        String notificationTitle =
                storeName
                + "에서 "
                + (
                    "FLAVOR".equals(
                            requestTargetType
                    )
                    ? "아이스크림 맛 재고"
                    : "재고"
                )
                + " 부족 신청이 접수되었습니다.";

        headNotificationService
                .createNotification(
                        NotificationCategory.INVENTORY,
                        notificationType,
                        notificationTitle,
                        "재고 신청 관리 화면에서 "
                        + "신청 내역을 확인해주세요.",
                        "head-inventory-requests",
                        String.valueOf(
                                requestId
                        )
                );

        /*
         * 본사 재고 신청 관리 화면에
         * 실시간 WebSocket 전송
         */
        restockRequestSocketPublisher
                .publishCreated(
                        requestId,
                        storeId,
                        storeName,
                        requestTargetType,
                        itemName,
                        dto.getRequestQuantity()
                );

        return "발주 요청 완료";
    }

    /*
     * 지점별 발주 신청 내역
     */
    @Transactional(readOnly = true)
    /**
     * [메서드 흐름] getRestockList
     * Controller 또는 상위 서비스에서 호출되어 BranchRestockMapper, BranchRestockRequestMapper, HeadRestockRequestMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<BranchRestockListResponseDTO>
            getRestockList(
                    Integer storeId
            ) {

        return headRestockRequestMapper
                .findByStoreIdOrderByIdDesc(
                        storeId
                )
                .stream()
                .map(request -> {

                    String itemName = "";
                    String unit = "";
                    if (request.getStoreInventory() != null) {
                        itemName = request.getStoreInventory().getItem().getItemName();
                        unit = request.getStoreInventory().getItem().getUnit();
                    } else if (request.getStoreFlavor() != null) {
                        itemName = request.getStoreFlavor().getFlavor().getFlavorName();
                        unit = "EA";
                    }

                    String adminName =
                            null;

                    if (
                            request.getStatus()
                                    == RestockStatus.CANCELED
                    ) {

                        adminName =
                                "지점 취소";

                    } else if (
                            request.getAdmin()
                                    != null
                    ) {

                        adminName =
                                request.getAdmin()
                                        .getName();
                    }

                    return BranchRestockListResponseDTO
                            .builder()
                            .requestId(
                                    request.getId()
                            )
                            .storeInventoryId(
                                    request
                                            .getStoreInventoryId()
                            )
                            .storeFlavorId(
                                    request
                                            .getStoreFlavorId()
                            )
                            .itemName(
                                    itemName
                            )
                            .unit(
                                    unit
                            )
                            .requestQuantity(
                                    request
                                            .getRequestQuantity()
                            )
                            .status(
                                    request.getStatus()
                            )
                            .adminId(
                                    request.getAdmin()
                                            != null
                                            ? request.getAdmin()
                                                    .getId()
                                            : null
                            )
                            .adminName(
                                    adminName
                            )
                            .requestedAt(
                                    request.getRequestedAt()
                            )
                            .build();
                })
                .toList();
    }

    /*
     * 지점 재고 신청 취소
     */
    /**
     * [메서드 흐름] cancelRestock
     * Controller 또는 상위 서비스에서 호출되어 BranchRestockMapper, BranchRestockRequestMapper, HeadRestockRequestMapper 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public String cancelRestock(
            Integer requestId
    ) {

        RestockRequest request =
                headRestockRequestMapper
                        .findById(
                                requestId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "존재하지 않는 발주 요청입니다."
                                )
                        );

        request.cancel();

        return "발주 요청 취소 성공";
    }

    private void validateRequest(
            BranchRestockRequestDTO dto
    ) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "재고 신청 정보가 없습니다."
            );
        }

        if (
                dto.getRequestQuantity() == null ||
                dto.getRequestQuantity() <= 0
        ) {
            throw new IllegalArgumentException(
                    "발주 수량은 1개 이상이어야 합니다."
            );
        }

        boolean hasInventory =
                dto.getStoreInventoryId()
                        != null;

        boolean hasFlavor =
                dto.getStoreFlavorId()
                        != null;

        if (
                hasInventory ==
                hasFlavor
        ) {
            throw new IllegalArgumentException(
                    "상품 재고와 맛 재고 중 "
                    + "하나만 선택해야 합니다."
            );
        }
    }

    private void validateStoreInventory(
            StoreInventory inventory
    ) {

        if (
                inventory.getStore() == null ||
                inventory.getStore()
                        .getId() == null
        ) {
            throw new IllegalArgumentException(
                    "상품 재고의 지점 정보가 없습니다."
            );
        }

        if (
                inventory.getItem() == null ||
                inventory.getItem()
                        .getId() == null
        ) {
            throw new IllegalArgumentException(
                    "상품 재고 품목 정보가 없습니다."
            );
        }
    }

    private void validateStoreFlavor(
            StoreFlavor flavor
    ) {

        if (
                flavor.getStore() == null ||
                flavor.getStore()
                        .getId() == null
        ) {
            throw new IllegalArgumentException(
                    "맛 재고의 지점 정보가 없습니다."
            );
        }

        if (
                flavor.getFlavor() == null ||
                flavor.getFlavor()
                        .getId() == null
        ) {
            throw new IllegalArgumentException(
                    "맛 재고 품목 정보가 없습니다."
            );
        }
    }

    private String getStoreName(
            String storeName
    ) {

        if (
                storeName == null ||
                storeName.isBlank()
        ) {
            return "지점";
        }

        return storeName.trim();
    }

    private String getItemName(
            String itemName
    ) {

        if (
                itemName == null ||
                itemName.isBlank()
        ) {
            return "재고 품목";
        }

        return itemName.trim();
    }
}
