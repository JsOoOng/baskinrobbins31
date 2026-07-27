package com.kiosk.headquarter.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreStatusHistory;

/**
 * 지점 상태 변경 이력을 저장하고 특정 비교 시점의 마지막 상태를 조회한다.
 */
public interface StoreStatusHistoryRepository
        extends JpaRepository<StoreStatusHistory, Long> {

    boolean existsByStore_Id(Integer storeId);

    Optional<StoreStatusHistory>
            findTopByStore_IdAndChangedAtLessThanEqualOrderByChangedAtDesc(
                    Integer storeId,
                    LocalDateTime changedAt
            );
}
