package com.kiosk.branch.status.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * [코드 흐름 안내] BranchRestockStatusSocketMessage
 *
 * <p>역할: 재입고·발주 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
public class BranchRestockStatusSocketMessage {

    /*
     * RESTOCK_REQUEST_APPROVED
     * RESTOCK_REQUEST_REJECTED
     */
    private String notificationType;

    /*
     * 화면에는 표시하지 않습니다.
     * 상세 조회나 내부 처리에만 사용합니다.
     */
    private Integer requestId;
    private Integer storeId;

    /*
     * INVENTORY 또는 FLAVOR
     */
    private String requestTargetType;

    private Integer itemId;
    private String itemName;
    private Integer requestQuantity;
    private String rejectionReason;

    private RestockStatus status;

    private String source;
    private String title;
    private String message;
    private String routeName;

    private LocalDateTime processedAt;

    public static BranchRestockStatusSocketMessage from(
            RestockRequest restockRequest
    ) {

        if (restockRequest == null) {
            throw new IllegalArgumentException(
                    "재고 신청 정보가 없습니다."
            );
        }

        RestockStatus status =
                restockRequest.getStatus();

        if (
                status != RestockStatus.APPROVED &&
                status != RestockStatus.REJECTED
        ) {
            throw new IllegalStateException(
                    "승인 또는 반려 상태만 알림으로 전송할 수 있습니다."
            );
        }

        Integer storeId;
        Integer itemId;
        String requestTargetType;
        String itemName;

        /*
         * 일반 재고 신청
         */
        if (
                restockRequest.getStoreInventory()
                        != null
        ) {

            if (
                    restockRequest
                            .getStoreInventory()
                            .getStore() == null
            ) {
                throw new IllegalStateException(
                        "재고 신청의 지점 정보가 없습니다."
                );
            }

            storeId =
                    restockRequest
                            .getStoreInventory()
                            .getStore()
                            .getId();

            requestTargetType =
                    "INVENTORY";
            itemId = restockRequest.getStoreInventory().getItem() == null
                    ? null
                    : restockRequest.getStoreInventory().getItem().getId();

            if (
                    restockRequest
                            .getStoreInventory()
                            .getItem() == null
            ) {
                itemName =
                        "재고 품목";
            } else {
                itemName =
                        normalize(
                                restockRequest
                                        .getStoreInventory()
                                        .getItem()
                                        .getItemName(),
                                "재고 품목"
                        );
            }

        }

        /*
         * 맛 재고 신청
         */
        else if (
                restockRequest.getStoreFlavor()
                        != null
        ) {

            if (
                    restockRequest
                            .getStoreFlavor()
                            .getStore() == null
            ) {
                throw new IllegalStateException(
                        "맛 재고 신청의 지점 정보가 없습니다."
                );
            }

            storeId =
                    restockRequest
                            .getStoreFlavor()
                            .getStore()
                            .getId();

            requestTargetType =
                    "FLAVOR";
            itemId = restockRequest.getStoreFlavor().getFlavor() == null
                    ? null
                    : restockRequest.getStoreFlavor().getFlavor().getId();

            if (
                    restockRequest
                            .getStoreFlavor()
                            .getFlavor() == null
            ) {
                itemName =
                        "맛 재고";
            } else {
                itemName =
                        normalize(
                                restockRequest
                                        .getStoreFlavor()
                                        .getFlavor()
                                        .getFlavorName(),
                                "맛 재고"
                        );
            }

        } else {
            throw new IllegalStateException(
                    "재고 신청 대상 정보가 없습니다."
            );
        }

        boolean approved =
                status == RestockStatus.APPROVED;

        String notificationType =
                approved
                        ? "RESTOCK_REQUEST_APPROVED"
                        : "RESTOCK_REQUEST_REJECTED";

        String title =
                approved
                        ? "재고 신청이 승인되었습니다."
                        : "재고 신청이 반려되었습니다.";

        String message =
                itemName
                + " "
                + restockRequest.getRequestQuantity()
                + "개 신청이 "
                + (
                    approved
                            ? "승인되었습니다."
                            : "반려되었습니다."
                );

        return BranchRestockStatusSocketMessage
                .builder()
                .notificationType(
                        notificationType
                )
                .requestId(
                        restockRequest.getId()
                )
                .storeId(
                        storeId
                )
                .requestTargetType(
                        requestTargetType
                )
                .itemName(
                        itemName
                )
                .itemId(
                        itemId
                )
                .requestQuantity(
                        restockRequest.getRequestQuantity()
                )
                .rejectionReason(
                        restockRequest.getRejectionReason()
                )
                .status(
                        status
                )
                .source(
                        "HEADQUARTER"
                )
                .title(
                        title
                )
                .message(
                        message
                )
                .routeName(
                        "branch-inventory"
                )
                .processedAt(
                        LocalDateTime.now()
                )
                .build();
    }

    private static String normalize(
            String value,
            String fallback
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {
            return fallback;
        }

        return value.trim();
    }
}
