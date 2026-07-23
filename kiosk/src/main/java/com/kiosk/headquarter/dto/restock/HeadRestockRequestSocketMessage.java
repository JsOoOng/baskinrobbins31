package com.kiosk.headquarter.dto.restock;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeadRestockRequestSocketMessage {

    private String notificationType;

    /*
     * 화면에는 requestId를 출력하지 않습니다.
     * 신청 상세 조회에만 사용합니다.
     */
    private Integer requestId;

    private Integer storeId;
    private String storeName;

    /*
     * INVENTORY 또는 FLAVOR
     */
    private String requestTargetType;

    private String itemName;
    private Integer requestQuantity;

    private String title;
    private String message;
    private String routeName;

    private LocalDateTime requestedAt;

    public static HeadRestockRequestSocketMessage create(
            Integer requestId,
            Integer storeId,
            String storeName,
            String requestTargetType,
            String itemName,
            Integer requestQuantity
    ) {

        boolean flavorRequest =
                "FLAVOR".equals(
                        requestTargetType
                );

        String notificationType =
                flavorRequest
                        ? "FLAVOR_RESTOCK_REQUEST_CREATED"
                        : "RESTOCK_REQUEST_CREATED";

        String targetName =
                flavorRequest
                        ? "아이스크림 맛 재고"
                        : "재고";

        return HeadRestockRequestSocketMessage
                .builder()
                .notificationType(
                        notificationType
                )
                .requestId(
                        requestId
                )
                .storeId(
                        storeId
                )
                .storeName(
                        normalize(
                                storeName,
                                "지점"
                        )
                )
                .requestTargetType(
                        requestTargetType
                )
                .itemName(
                        normalize(
                                itemName,
                                "재고 품목"
                        )
                )
                .requestQuantity(
                        requestQuantity
                )
                .title(
                        normalize(
                                storeName,
                                "지점"
                        )
                        + "에서 "
                        + targetName
                        + " 부족 신청이 접수되었습니다."
                )
                .message(
                        "재고 신청 관리 화면에서 신청 내역을 확인해주세요."
                )
                .routeName(
                        "head-inventory-requests"
                )
                .requestedAt(
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