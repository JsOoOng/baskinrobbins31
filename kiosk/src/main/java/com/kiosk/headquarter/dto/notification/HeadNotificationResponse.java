package com.kiosk.headquarter.dto.notification;

import java.time.LocalDateTime;

import com.kiosk.entity.HeadNotification;
import com.kiosk.entity.enums.NotificationCategory;
import com.kiosk.entity.enums.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] HeadNotificationResponse
 *
 * <p>역할: 알림 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeadNotificationResponse {

    private Integer notificationId;

    private NotificationCategory category;

    private NotificationType notificationType;

    private String title;

    private String message;

    /*
     * Vue Router 이름
     *
     * 예:
     * head-inventory
     * head-coupons
     * head-events
     * head-banners
     */
    private String routeName;

    /*
     * 관련 데이터 식별값
     *
     * 재고 번호, 쿠폰 ID,
     * 이벤트 번호, 배너 번호
     */
    private String referenceKey;

    private Boolean isRead;

    private LocalDateTime createdAt;

    public static HeadNotificationResponse from(
            HeadNotification notification,
            boolean isRead
    ) {

        return HeadNotificationResponse.builder()
                .notificationId(
                        notification.getId()
                )
                .category(
                        notification.getCategory()
                )
                .notificationType(
                        notification
                                .getNotificationType()
                )
                .title(
                        notification.getTitle()
                )
                .message(
                        notification.getMessage()
                )
                .routeName(
                        notification.getRouteName()
                )
                .referenceKey(
                        notification.getReferenceKey()
                )
                .isRead(isRead)
                .createdAt(
                        notification.getCreatedAt()
                )
                .build();
    }
}