package com.kiosk.headquarter.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.HeadNotification;
import com.kiosk.entity.HeadNotificationRead;
import com.kiosk.entity.enums.NotificationCategory;
import com.kiosk.entity.enums.NotificationType;
import com.kiosk.headquarter.dto.notification.HeadNotificationResponse;
import com.kiosk.headquarter.dto.notification.HeadNotificationUnreadCountResponse;
import com.kiosk.headquarter.repository.HeadNotificationReadRepository;
import com.kiosk.headquarter.repository.HeadNotificationRepository;
import org.springframework.transaction.annotation.Propagation;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadNotificationService
 *
 * <p>역할: 본사 관리의 알림 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> HeadNotificationRepository, HeadNotificationReadRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadNotificationService {

    private final HeadNotificationRepository
            headNotificationRepository;

    private final HeadNotificationReadRepository
            headNotificationReadRepository;

    /*
     * 공통 알림 생성
     *
     * 재고, 쿠폰, 이벤트, 배너 Service에서
     * 이 메서드를 호출하게 됩니다.
     */
    @Transactional
    /**
     * [메서드 흐름] createNotification
     * Controller 또는 상위 서비스에서 호출되어 HeadNotificationRepository, HeadNotificationReadRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadNotificationResponse createNotification(
            NotificationCategory category,
            NotificationType notificationType,
            String title,
            String message,
            String routeName,
            String referenceKey
    ) {

        HeadNotification notification =
                HeadNotification.create(
                        category,
                        notificationType,
                        title,
                        message,
                        routeName,
                        referenceKey
                );

        HeadNotification savedNotification =
                headNotificationRepository.save(
                        notification
                );

        /*
         * 새로 생성된 알림은
         * 아직 어느 관리자도 읽지 않은 상태입니다.
         */
        return HeadNotificationResponse.from(
                savedNotification,
                false
        );
    }
    
    @Transactional
    /**
     * [메서드 흐름] createLowStockNotification
     * Controller 또는 상위 서비스에서 호출되어 HeadNotificationRepository, HeadNotificationReadRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadNotificationResponse
            createLowStockNotification(
                    Integer storeInventoryId,
                    String storeName,
                    String productName,
                    Integer currentStock,
                    Integer minStock
            ) {

        return createNotification(
                NotificationCategory.INVENTORY,
                NotificationType.LOW_STOCK,
                "재고 부족 발생",
                String.format(
                        "%s의 %s 재고가 부족합니다. 현재 %d개, 최소 %d개",
                        normalizeDisplayName(
                                storeName,
                                "지점명 없음"
                        ),
                        normalizeDisplayName(
                                productName,
                                "상품명 없음"
                        ),
                        currentStock == null
                                ? 0
                                : currentStock,
                        minStock == null
                                ? 0
                                : minStock
                ),
                "head-inventory",
                String.valueOf(
                        storeInventoryId
                )
        );
    }
    
    @Transactional
    /**
     * [메서드 흐름] createRestockRequestNotification
     * Controller 또는 상위 서비스에서 호출되어 HeadNotificationRepository, HeadNotificationReadRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadNotificationResponse
            createRestockRequestNotification(
                    String storeName,
                    String productName,
                    Integer requestQuantity
            ) {

        return createNotification(
                NotificationCategory.INVENTORY,
                NotificationType.RESTOCK_REQUEST,
                "재고 신청 발생",
                String.format(
                        "%s에서 %s(을)를 %d개 신청했습니다.",
                        normalizeDisplayName(
                                storeName,
                                "지점명 없음"
                        ),
                        normalizeDisplayName(
                                productName,
                                "상품명 없음"
                        ),
                        requestQuantity == null
                                ? 0
                                : requestQuantity
                ),
                "head-inventory-requests",
                null
        );
    }
    
    @Transactional
    /**
     * [메서드 흐름] createAutoRestockSuccessNotification
     * Controller 또는 상위 서비스에서 호출되어 HeadNotificationRepository, HeadNotificationReadRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadNotificationResponse
            createAutoRestockSuccessNotification(
                    Integer storeInventoryId,
                    String storeName,
                    String productName,
                    Integer replenishedQuantity,
                    Integer currentStock
            ) {

        return createNotification(
                NotificationCategory.INVENTORY,
                NotificationType.AUTO_RESTOCK_SUCCESS,
                "자동 재고 보충 완료",
                String.format(
                        "%s의 %s 재고가 %d개 보충되어 현재 %d개가 되었습니다.",
                        normalizeDisplayName(
                                storeName,
                                "지점명 없음"
                        ),
                        normalizeDisplayName(
                                productName,
                                "상품명 없음"
                        ),
                        replenishedQuantity == null
                                ? 0
                                : replenishedQuantity,
                        currentStock == null
                                ? 0
                                : currentStock
                ),
                "head-inventory",
                String.valueOf(
                        storeInventoryId
                )
        );
    }
    
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    /**
     * [메서드 흐름] createAutoRestockFailureNotification
     * Controller 또는 상위 서비스에서 호출되어 HeadNotificationRepository, HeadNotificationReadRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadNotificationResponse
            createAutoRestockFailureNotification(
                    Integer storeInventoryId,
                    String storeName,
                    String productName,
                    String failureReason
            ) {

        return createNotification(
                NotificationCategory.INVENTORY,
                NotificationType.AUTO_RESTOCK_FAILED,
                "자동 재고 보충 실패",
                String.format(
                        "%s의 %s 자동 재고 보충에 실패했습니다. 사유: %s",
                        normalizeDisplayName(
                                storeName,
                                "지점명 없음"
                        ),
                        normalizeDisplayName(
                                productName,
                                "상품명 없음"
                        ),
                        normalizeDisplayName(
                                failureReason,
                                "알 수 없는 오류"
                        )
                ),
                "head-inventory",
                String.valueOf(
                        storeInventoryId
                )
        );
    }

    /*
     * 관리자별 전체 알림 목록 조회
     *
     * 최신 알림이 먼저 반환됩니다.
     */
    /**
     * [메서드 흐름] getNotifications
     * Controller 또는 상위 서비스에서 호출되어 HeadNotificationRepository, HeadNotificationReadRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadNotificationResponse>
            getNotifications(
                    Integer adminId
            ) {

        validateAdminId(adminId);

        Set<Integer> readNotificationIds =
                new HashSet<>(
                        headNotificationReadRepository
                                .findReadNotificationIds(
                                        adminId
                                )
                );

        return headNotificationRepository
                .findAllByOrderByCreatedAtDescIdDesc()
                .stream()
                .map(notification ->
                        HeadNotificationResponse.from(
                                notification,
                                readNotificationIds.contains(
                                        notification.getId()
                                )
                        )
                )
                .toList();
    }

    /*
     * 읽지 않은 알림 개수 조회
     */
    /**
     * [메서드 흐름] getUnreadCount
     * Controller 또는 상위 서비스에서 호출되어 HeadNotificationRepository, HeadNotificationReadRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadNotificationUnreadCountResponse
            getUnreadCount(
                    Integer adminId
            ) {

        validateAdminId(adminId);

        long totalNotificationCount =
                headNotificationRepository.count();

        long readNotificationCount =
                headNotificationReadRepository
                        .countByAdminId(
                                adminId
                        );

        long unreadCount =
                Math.max(
                        0,
                        totalNotificationCount -
                                readNotificationCount
                );

        return new HeadNotificationUnreadCountResponse(
                unreadCount
        );
    }

    /*
     * 개별 알림 읽음 처리
     */
    @Transactional
    /**
     * [메서드 흐름] markAsRead
     * Controller 또는 상위 서비스에서 호출되어 HeadNotificationRepository, HeadNotificationReadRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadNotificationResponse markAsRead(
            Integer notificationId,
            Integer adminId
    ) {

        validateAdminId(adminId);

        HeadNotification notification =
                findNotification(
                        notificationId
                );

        boolean alreadyRead =
                headNotificationReadRepository
                        .existsByNotification_IdAndAdminId(
                                notificationId,
                                adminId
                        );

        /*
         * 이미 읽은 알림이면
         * 읽음 데이터를 중복 저장하지 않습니다.
         */
        if (!alreadyRead) {

            HeadNotificationRead notificationRead =
                    HeadNotificationRead.create(
                            notification,
                            adminId
                    );

            headNotificationReadRepository.save(
                    notificationRead
            );
        }

        return HeadNotificationResponse.from(
                notification,
                true
        );
    }

    /*
     * 전체 알림 읽음 처리
     */
    @Transactional
    /**
     * [메서드 흐름] markAllAsRead
     * Controller 또는 상위 서비스에서 호출되어 HeadNotificationRepository, HeadNotificationReadRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadNotificationUnreadCountResponse
            markAllAsRead(
                    Integer adminId
            ) {

        validateAdminId(adminId);

        Set<Integer> readNotificationIds =
                new HashSet<>(
                        headNotificationReadRepository
                                .findReadNotificationIds(
                                        adminId
                                )
                );

        List<HeadNotificationRead> newReadRecords =
                headNotificationRepository
                        .findAll()
                        .stream()
                        .filter(notification ->
                                !readNotificationIds.contains(
                                        notification.getId()
                                )
                        )
                        .map(notification ->
                                HeadNotificationRead.create(
                                        notification,
                                        adminId
                                )
                        )
                        .toList();

        if (!newReadRecords.isEmpty()) {
            headNotificationReadRepository
                    .saveAll(
                            newReadRecords
                    );
        }

        return new HeadNotificationUnreadCountResponse(
                0
        );
    }

    /*
     * 알림 조회 공통 처리
     */
    private HeadNotification findNotification(
            Integer notificationId
    ) {

        if (notificationId == null) {
            throw new IllegalArgumentException(
                    "알림 번호가 없습니다."
            );
        }

        return headNotificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "존재하지 않는 알림입니다."
                        )
                );
    }

    /*
     * 관리자 번호 검증
     */
    private void validateAdminId(
            Integer adminId
    ) {

        if (adminId == null) {
            throw new IllegalArgumentException(
                    "관리자 번호가 없습니다."
            );
        }

        if (adminId <= 0) {
            throw new IllegalArgumentException(
                    "올바르지 않은 관리자 번호입니다."
            );
        }
    }
    
    private String normalizeDisplayName(
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