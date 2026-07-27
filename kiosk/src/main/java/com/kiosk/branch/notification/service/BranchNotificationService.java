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

    /*
     * 기존 알림을 다시 읽지 않음 상태로 바꾸고 같은 내용을 실시간 재전송합니다.
     * 고유키를 유지하므로 재발송할 때 DB 행이 중복 생성되지 않습니다.
     */
    @Transactional
    public void resend(Integer storeId, String type, String referenceKey,
                       String title, String message) {
        BranchNotification notification = repository
                .findByStoreIdAndNotificationTypeAndReferenceKey(storeId, type, referenceKey)
                .orElseThrow(() -> new IllegalArgumentException("재발송할 지점 알림을 찾을 수 없습니다."));
        notification.markUnread();
        BranchNotification saved = repository.saveAndFlush(notification);
        messagingTemplate.convertAndSend(
                "/topic/stores/" + storeId + "/notifications",
                BranchNotificationResponse.from(saved));
    }

    @Transactional
    public void markRead(Integer storeId, Integer notificationId) {
        repository.findByIdAndStoreId(notificationId, storeId)
                .orElseThrow(() -> new IllegalArgumentException("지점 알림을 찾을 수 없습니다."))
                .markRead();
    }
}
