package com.kiosk.branch.notification.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.notification.dto.BranchNotificationResponse;
import com.kiosk.branch.notification.repository.BranchNotificationRepository;
import com.kiosk.entity.BranchNotification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchNotificationService {

    private final BranchNotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void createOnce(Integer storeId, String type, String referenceKey,
                           String title, String message) {
        if (repository.existsByStoreIdAndNotificationTypeAndReferenceKey(storeId, type, referenceKey)) {
            return;
        }
        try {
            BranchNotification saved = repository.saveAndFlush(BranchNotification.builder()
                    .storeId(storeId)
                    .notificationType(type)
                    .referenceKey(referenceKey)
                    .title(title)
                    .message(message)
                    .build());
            messagingTemplate.convertAndSend(
                    "/topic/stores/" + storeId + "/notifications",
                    BranchNotificationResponse.from(saved));
        } catch (DataIntegrityViolationException ignored) {
            // 동시에 같은 상태가 처리되어도 고유키가 중복 알림을 막는다.
        }
    }

    @Transactional(readOnly = true)
    public List<BranchNotificationResponse> getUnread(Integer storeId) {
        return repository.findAllByStoreIdAndIsReadFalseOrderByCreatedAtDescIdDesc(storeId)
                .stream().map(BranchNotificationResponse::from).toList();
    }

    @Transactional
    public void markRead(Integer storeId, Integer notificationId) {
        repository.findByIdAndStoreId(notificationId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("지점 알림을 찾을 수 없습니다."))
                .markRead();
    }
}
