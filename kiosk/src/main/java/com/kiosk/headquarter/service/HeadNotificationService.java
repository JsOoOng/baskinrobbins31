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