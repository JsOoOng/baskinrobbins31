package com.kiosk.headquarter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.common.websocket.InventoryAlertSocketPublisher;
import com.kiosk.entity.InventoryShortageAlert;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.InventoryShortageAlertStatus;
import com.kiosk.headquarter.dto.inventoryalert.HeadInventoryShortageSocketResponse;
import com.kiosk.branch.inventory.dto.BranchInventoryShortageConfirmResponse;
import com.kiosk.branch.inventory.dto.BranchInventoryShortageSummaryResponse;

import com.kiosk.headquarter.repository.InventoryShortageAlertRepository;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] InventoryShortageAlertService
 *
 * <p>역할: 본사 관리의 재고 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> InventoryShortageAlertRepository, InventoryAlertSocketPublisher -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryShortageAlertService {

    /*
     * RESOLVED를 제외한 활성 상태
     */
    private static final List<
            InventoryShortageAlertStatus
    > ACTIVE_STATUSES =
            List.of(
                    InventoryShortageAlertStatus
                            .DETECTED,

                    InventoryShortageAlertStatus
                            .SENT,

                    InventoryShortageAlertStatus
                            .CONFIRMED,

                    InventoryShortageAlertStatus
                            .REQUESTED
            );

    /*
     * 본사가 아직 지점에 보내지 않은 상태
     */
    private static final List<
            InventoryShortageAlertStatus
    > HEAD_DETECTED_STATUSES =
            List.of(
                    InventoryShortageAlertStatus
                            .DETECTED
            );

    /*
     * 지점에서 확인해야 하는 부족 알림 상태
     *
     * DETECTED:
     * 시스템이 자동으로 만든 지점 자체 알림
     *
     * SENT:
     * 본사가 지점에 별도로 전송한 알림
     */
    private static final List<
            InventoryShortageAlertStatus
    > BRANCH_PENDING_STATUSES =
            List.of(
                    InventoryShortageAlertStatus
                            .DETECTED,

                    InventoryShortageAlertStatus
                            .SENT
            );

    private final InventoryShortageAlertRepository
            inventoryShortageAlertRepository;

    private final InventoryAlertSocketPublisher
            inventoryAlertSocketPublisher;
    private final AdminLogService adminLogService;

    /*
     * 재고 부족 감지 또는 기존 알람 갱신
     */
    @Transactional
    /**
     * [메서드 흐름] detectOrRefreshShortage
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public Optional<InventoryShortageAlert>
            detectOrRefreshShortage(
                    StoreInventory inventory
            ) {

        validateInventory(
                inventory
        );

        Optional<InventoryShortageAlert>
                activeAlertOptional =
                findActiveAlert(
                        inventory.getId()
                );

        /*
         * 현재 재고가 정상 범위인 경우
         */
        if (!inventory.isLowStock()) {

            if (activeAlertOptional.isPresent()) {

                InventoryShortageAlert alert =
                        activeAlertOptional.get();

                alert.resolve();

                inventoryShortageAlertRepository
                        .flush();

                /*
                 * RESOLVED 상태도 전송합니다.
                 *
                 * 본사 화면에서는 해당 상태를 받으면
                 * 부족 목록에서 제거할 수 있습니다.
                 */
                inventoryAlertSocketPublisher
                        .publishToHead(
                                alert
                        );
            }

            return activeAlertOptional;
        }

        /*
         * 기존 활성 알람이 있는 경우
         *
         * 새 행을 만들지 않고 부족 수량을 갱신합니다.
         */
        if (activeAlertOptional.isPresent()) {

            InventoryShortageAlert alert =
                    activeAlertOptional.get();

            alert.refreshShortage(
                    inventory
            );

            inventoryShortageAlertRepository
                    .flush();

            /*
             * 부족 수량 변경 내용을
             * 본사 화면에 실시간 전송합니다.
             */
            inventoryAlertSocketPublisher
                    .publishToHead(
                            alert
                    );

            return Optional.of(
                    alert
            );
        }

        /*
         * 활성 알람이 없다면 신규 생성
         */
        InventoryShortageAlert newAlert =
                InventoryShortageAlert.detect(
                        inventory
                );

        InventoryShortageAlert savedAlert =
                inventoryShortageAlertRepository
                        .saveAndFlush(
                                newAlert
                        );

        /*
         * 1. 본사에 자동 부족 알림
         *
         * 예:
         * 강남점에 부족한 재고가 있습니다.
         */
        inventoryAlertSocketPublisher
                .publishToHead(
                        savedAlert
                );

        /*
         * 2. 해당 지점에 자체 부족 알림
         *
         * 예:
         * 부족한 재고가 있습니다.
         */
        inventoryAlertSocketPublisher
                .publishSelfAlertToStore(
                        savedAlert
                );

        return Optional.of(
                savedAlert
        );
    }

    /*
     * 특정 재고의 활성 알람 조회
     */
    /**
     * [메서드 흐름] getActiveAlert
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public Optional<InventoryShortageAlert>
            getActiveAlert(
                    Integer storeInventoryId
            ) {

        validateStoreInventoryId(
                storeInventoryId
        );

        return findActiveAlert(
                storeInventoryId
        );
    }

    /*
     * 본사 재고 현황에 표시할
     * 전체 활성 알람 조회
     */
    /**
     * [메서드 흐름] getAllActiveAlerts
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<InventoryShortageAlert>
            getAllActiveAlerts() {

        return inventoryShortageAlertRepository
                .findAllByAlertStatusInOrderByDetectedAtDescIdDesc(
                        ACTIVE_STATUSES
                );
    }

    /*
     * 본사가 아직 지점에 보내지 않은
     * DETECTED 알람 조회
     */
    /**
     * [메서드 흐름] getDetectedAlerts
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<InventoryShortageAlert>
            getDetectedAlerts() {

        return inventoryShortageAlertRepository
                .findAllByAlertStatusInOrderByDetectedAtDescIdDesc(
                        HEAD_DETECTED_STATUSES
                );
    }

    /*
     * 지점 관리자가 확인해야 하는
     * SENT 알람 조회
     */
    /**
     * [메서드 흐름] getPendingBranchAlerts
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<InventoryShortageAlert>
            getPendingBranchAlerts(
                    Integer storeId
            ) {

        validateStoreId(
                storeId
        );

        return inventoryShortageAlertRepository
                .findAllByStore_IdAndAlertStatusInOrderByDetectedAtDescIdDesc(
                        storeId,
                        BRANCH_PENDING_STATUSES
                );
    }

    /*
     * 특정 지점의 특정 알람 조회
     */
    /**
     * [메서드 흐름] getStoreAlert
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public InventoryShortageAlert
            getStoreAlert(
                    Integer alertId,
                    Integer storeId
            ) {

        validateAlertId(
                alertId
        );

        validateStoreId(
                storeId
        );

        return inventoryShortageAlertRepository
                .findByIdAndStore_Id(
                        alertId,
                        storeId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 지점의 재고 부족 알람이 없습니다."
                        )
                );
    }

    /*
     * 활성 알람 내부 조회
     */
    private Optional<InventoryShortageAlert>
            findActiveAlert(
                    Integer storeInventoryId
            ) {

        return inventoryShortageAlertRepository
                .findFirstByStoreInventory_IdAndAlertStatusInOrderByDetectedAtDescIdDesc(
                        storeInventoryId,
                        ACTIVE_STATUSES
                );
    }

    /*
     * 지점 재고 정보 검증
     */
    private void validateInventory(
            StoreInventory inventory
    ) {

        if (inventory == null) {
            throw new IllegalArgumentException(
                    "지점 재고 정보가 없습니다."
            );
        }

        validateStoreInventoryId(
                inventory.getId()
        );

        if (inventory.getStore() == null) {
            throw new IllegalArgumentException(
                    "재고에 연결된 지점 정보가 없습니다."
            );
        }

        if (
                inventory.getStore()
                        .getId() == null
        ) {
            throw new IllegalArgumentException(
                    "재고에 연결된 지점 번호가 없습니다."
            );
        }

        if (inventory.getItem() == null) {
            throw new IllegalArgumentException(
                    "재고 품목 정보가 없습니다."
            );
        }

        if (
                inventory.getItem()
                        .getId() == null
        ) {
            throw new IllegalArgumentException(
                    "재고 품목 번호가 없습니다."
            );
        }

        if (
                inventory.getCurrentStock()
                        == null
        ) {
            throw new IllegalArgumentException(
                    "현재 재고 정보가 없습니다."
            );
        }

        if (
                inventory.getMinStock()
                        == null
        ) {
            throw new IllegalArgumentException(
                    "최소 재고 정보가 없습니다."
            );
        }
    }

    private void validateStoreInventoryId(
            Integer storeInventoryId
    ) {

        if (
                storeInventoryId == null ||
                storeInventoryId <= 0
        ) {
            throw new IllegalArgumentException(
                    "지점 재고 번호가 올바르지 않습니다."
            );
        }
    }

    private void validateStoreId(
            Integer storeId
    ) {

        if (
                storeId == null ||
                storeId <= 0
        ) {
            throw new IllegalArgumentException(
                    "지점 번호가 올바르지 않습니다."
            );
        }
    }

    private void validateAlertId(
            Integer alertId
    ) {

        if (
                alertId == null ||
                alertId <= 0
        ) {
            throw new IllegalArgumentException(
                    "알람 번호가 올바르지 않습니다."
            );
        }
    }
    
    /*
     * 본사 재고 현황 화면에 표시할
     * 활성 부족 알람 DTO 목록
     */
    /**
     * [메서드 흐름] getAllActiveAlertResponses
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<HeadInventoryShortageSocketResponse>
            getAllActiveAlertResponses() {

        return getAllActiveAlerts()
                .stream()
                .map(
                        HeadInventoryShortageSocketResponse
                                ::from
                )
                .toList();
    }
    
    /*
     * 본사 관리자가 부족 알람을
     * 해당 지점 관리자에게 전송합니다.
     */
    @Transactional
    /**
     * [메서드 흐름] sendAlertToStore
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public HeadInventoryShortageSocketResponse
            sendAlertToStore(
                    Integer alertId,
                    Integer adminId
            ) {

        validateAlertId(
                alertId
        );

        validateAdminId(
                adminId
        );

        InventoryShortageAlert alert =
                inventoryShortageAlertRepository
                        .findById(
                                alertId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "재고 부족 알람이 없습니다."
                                )
                        );

        /*
         * 이미 해결된 알람은
         * 지점에 보낼 수 없습니다.
         */
        if (!alert.isActiveAlert()) {
            throw new IllegalStateException(
                    "이미 해결된 재고 부족 알람입니다."
            );
        }

        /*
         * InventoryShortageAlert의 markSent에서
         * DETECTED 상태인지 다시 검사합니다.
         */
        alert.markSent(
                adminId
        );

        inventoryShortageAlertRepository
                .flush();

        /*
         * 본사 관리자가 직접 보내는
         * 별도의 지점 알림입니다.
         */
        inventoryAlertSocketPublisher
                .publishHeadReminderToStore(
                        alert
                );

        /*
         * 2. 본사 화면에도 SENT 상태 전송
         *
         * 본사 목록의 버튼과 상태를
         * 실시간으로 변경할 수 있습니다.
         */
        inventoryAlertSocketPublisher
                .publishToHead(
                        alert
                );

        adminLogService.logAction("재고 알림",
                alert.getStoreInventory().getStore().getStoreName() + " - "
                        + alert.getStoreInventory().getItem().getItemName() + " 부족 알림 전송");
        return HeadInventoryShortageSocketResponse
                .from(
                        alert
                );
    }
    
    /*
     * 본사 관리자 번호 검증
     */
    private void validateAdminId(
            Integer adminId
    ) {

        if (
                adminId == null ||
                adminId <= 0
        ) {
            throw new IllegalArgumentException(
                    "본사 관리자 번호가 올바르지 않습니다."
            );
        }
    }
    
    /*
     * 지점의 미확인 재고 부족 알림을
     * 한 개의 묶음 응답으로 반환합니다.
     *
     * DETECTED와 SENT 상태를 모두 조회합니다.
     */
    /**
     * [메서드 흐름] getPendingBranchAlertSummary
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public BranchInventoryShortageSummaryResponse
            getPendingBranchAlertSummary(
                    Integer storeId
            ) {

        validateStoreId(
                storeId
        );

        List<InventoryShortageAlert> alerts =
                inventoryShortageAlertRepository
                        .findAllByStore_IdAndAlertStatusInOrderByDetectedAtDescIdDesc(
                                storeId,
                                BRANCH_PENDING_STATUSES
                        );

        return BranchInventoryShortageSummaryResponse
                .from(
                        storeId,
                        alerts
                );
    }
    
    /*
     * 지점 관리자가 부족 알림 모달의
     * 확인 버튼을 눌렀을 때 실행합니다.
     *
     * 해당 지점의 DETECTED, SENT 알림을
     * 전부 CONFIRMED로 변경합니다.
     */
    @Transactional
    /**
     * [메서드 흐름] confirmPendingBranchAlerts
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public BranchInventoryShortageConfirmResponse
            confirmPendingBranchAlerts(
                    Integer storeId
            ) {

        validateStoreId(
                storeId
        );

        List<InventoryShortageAlert> alerts =
                inventoryShortageAlertRepository
                        .findAllByStore_IdAndAlertStatusInOrderByDetectedAtDescIdDesc(
                                storeId,
                                BRANCH_PENDING_STATUSES
                        );

        if (alerts.isEmpty()) {
            return new BranchInventoryShortageConfirmResponse(
                    storeId,
                    0,
                    "branch-inventory"
            );
        }

        for (
                InventoryShortageAlert alert
                : alerts
        ) {

            /*
             * DETECTED 또는 SENT
             * → CONFIRMED
             */
            alert.confirm();
        }

        inventoryShortageAlertRepository
                .flush();

        /*
         * 본사 화면에도 변경된 상태를 전송합니다.
         *
         * 본사 재고 현황에서
         * 지점 확인 여부를 갱신할 수 있습니다.
         */
        for (
                InventoryShortageAlert alert
                : alerts
        ) {

            inventoryAlertSocketPublisher
                    .publishToHead(
                            alert
                    );
        }

        return new BranchInventoryShortageConfirmResponse(
                storeId,
                alerts.size(),
                "branch-inventory"
        );
    }
    
    /*
     * 일반 재고 부족 알람을
     * 생성된 발주 신청과 연결합니다.
     *
     * CONFIRMED → REQUESTED
     */
    @Transactional
    /**
     * [메서드 흐름] markRequested
     * Controller 또는 상위 서비스에서 호출되어 InventoryShortageAlertRepository, InventoryAlertSocketPublisher을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public InventoryShortageAlert markRequested(
            Integer alertId,
            Integer storeInventoryId,
            Integer restockRequestId
    ) {

        validateAlertId(
                alertId
        );

        validateStoreInventoryId(
                storeInventoryId
        );

        if (
                restockRequestId == null ||
                restockRequestId <= 0
        ) {
            throw new IllegalArgumentException(
                    "재고 신청 번호가 올바르지 않습니다."
            );
        }

        InventoryShortageAlert alert =
                inventoryShortageAlertRepository
                        .findById(
                                alertId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "재고 부족 알람이 없습니다."
                                )
                        );

        if (
                alert.getStoreInventory() == null ||
                alert.getStoreInventory()
                        .getId() == null ||
                !alert.getStoreInventory()
                        .getId()
                        .equals(
                                storeInventoryId
                        )
        ) {
            throw new IllegalArgumentException(
                    "재고 부족 알람과 신청 재고가 일치하지 않습니다."
            );
        }

        /*
         * 엔티티 내부에서 CONFIRMED 상태인지 검사합니다.
         */
        alert.markRequested(
                restockRequestId
        );

        inventoryShortageAlertRepository
                .flush();

        /*
         * 본사 재고 현황 화면에
         * REQUESTED 상태를 실시간 반영합니다.
         */
        inventoryAlertSocketPublisher
                .publishToHead(
                        alert
                );

        return alert;
    }
}
