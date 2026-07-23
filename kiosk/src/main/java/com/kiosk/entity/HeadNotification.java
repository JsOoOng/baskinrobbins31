package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.NotificationCategory;
import com.kiosk.entity.enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] HeadNotification
 *
 * <p>역할: 알림 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 head_notifications 테이블 매핑을 통해 이 객체를 저장·조회한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "head_notifications")
@Getter
@Builder
@NoArgsConstructor(
        access = AccessLevel.PROTECTED
)
@AllArgsConstructor
public class HeadNotification {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "notification_id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "category",
            length = 30,
            nullable = false
    )
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "notification_type",
            length = 50,
            nullable = false
    )
    private NotificationType notificationType;

    @Column(
            name = "title",
            length = 100,
            nullable = false
    )
    private String title;

    @Column(
            name = "message",
            length = 500,
            nullable = false
    )
    private String message;

    /*
     * Vue Router에 등록된 이름
     *
     * 예:
     * head-inventory
     * head-coupons
     * head-events
     * head-banners
     */
    @Column(
            name = "route_name",
            length = 100
    )
    private String routeName;

    /*
     * 재고 번호, 쿠폰 ID,
     * 이벤트 번호, 배너 번호
     */
    @Column(
            name = "reference_key",
            length = 100
    )
    private String referenceKey;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    /*
     * 공통 알림 생성
     */
    public static HeadNotification create(
            NotificationCategory category,
            NotificationType notificationType,
            String title,
            String message,
            String routeName,
            String referenceKey
    ) {

        if (category == null) {
            throw new IllegalArgumentException(
                    "알림 분류가 없습니다."
            );
        }

        if (notificationType == null) {
            throw new IllegalArgumentException(
                    "알림 종류가 없습니다."
            );
        }

        if (
                title == null ||
                title.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "알림 제목이 없습니다."
            );
        }

        if (
                message == null ||
                message.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "알림 내용이 없습니다."
            );
        }

        return HeadNotification.builder()
                .category(category)
                .notificationType(
                        notificationType
                )
                .title(title.trim())
                .message(message.trim())
                .routeName(
                        normalizeNullable(
                                routeName
                        )
                )
                .referenceKey(
                        normalizeNullable(
                                referenceKey
                        )
                )
                .build();
    }

    private static String normalizeNullable(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}