package com.kiosk.branch.notification.dto;

import java.time.LocalDateTime;

import com.kiosk.entity.BranchNotification;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BranchNotificationResponse {
    private Integer notificationId;
    private Integer storeId;
    private String notificationType;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static BranchNotificationResponse from(BranchNotification notification) {
        return BranchNotificationResponse.builder()
                .notificationId(notification.getId())
                .storeId(notification.getStoreId())
                .notificationType(notification.getNotificationType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
