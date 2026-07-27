package com.kiosk.branch.notification.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.BranchNotification;

public interface BranchNotificationRepository extends JpaRepository<BranchNotification, Integer> {
    boolean existsByStoreIdAndNotificationTypeAndReferenceKey(
            Integer storeId, String notificationType, String referenceKey);
    List<BranchNotification> findAllByStoreIdAndIsReadFalseOrderByCreatedAtDescIdDesc(Integer storeId);
    Optional<BranchNotification> findByIdAndStoreId(Integer id, Integer storeId);
    Optional<BranchNotification> findByStoreIdAndNotificationTypeAndReferenceKey(
            Integer storeId, String notificationType, String referenceKey);
}
