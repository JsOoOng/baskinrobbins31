package com.kiosk.branch.status.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.enums.RestockStatus;

import lombok.Builder;
import lombok.Getter;

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

    private String itemName;
    private Integer requestQuantity;

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
                .requestQuantity(
                        restockRequest.getRequestQuantity()
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