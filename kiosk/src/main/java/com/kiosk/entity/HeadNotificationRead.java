package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] HeadNotificationRead
 *
 * <p>역할: 알림 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 head_notification_reads 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 notification_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(
        name = "head_notification_reads",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_head_notification_read",
                        columnNames = {
                                "notification_id",
                                "admin_id"
                        }
                )
        }
)
@Getter
@Builder
@NoArgsConstructor(
        access = AccessLevel.PROTECTED
)
@AllArgsConstructor
public class HeadNotificationRead {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "notification_read_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "notification_id",
            nullable = false
    )
    private HeadNotification notification;

    /*
     * headquarter_admins.admin_id
     *
     * DB에는 외래키가 설정되어 있지만
     * 현재 본사 관리자 JPA 엔티티가 없으므로
     * Integer로 보관합니다.
     */
    @Column(
            name = "admin_id",
            nullable = false
    )
    private Integer adminId;

    @CreationTimestamp
    @Column(
            name = "read_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime readAt;

    public static HeadNotificationRead create(
            HeadNotification notification,
            Integer adminId
    ) {

        if (notification == null) {
            throw new IllegalArgumentException(
                    "읽음 처리할 알림이 없습니다."
            );
        }

        if (adminId == null) {
            throw new IllegalArgumentException(
                    "관리자 번호가 없습니다."
            );
        }

        return HeadNotificationRead.builder()
                .notification(notification)
                .adminId(adminId)
                .build();
    }
}