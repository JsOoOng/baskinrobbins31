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
import com.kiosk.headquarter.repository.InventoryShortageAlertRepository;

import lombok.RequiredArgsConstructor;

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
     * 지점 관리자가 확인해야 하는 상태
     */
    private static final List<
            InventoryShortageAlertStatus
    > BRANCH_PENDING_STATUSES =
            List.of(
                    InventoryShortageAlertStatus
                            .SENT
            );

    private final InventoryShortageAlertRepository
            inventoryShortageAlertRepository;

    private final InventoryAlertSocketPublisher
            inventoryAlertSocketPublisher;

    /*
     * 재고 부족 감지 또는 기존 알람 갱신
     */
    @Transactional
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
         * 신규 DETECTED 알람을
         * 본사 재고 현황 화면으로 전송합니다.
         */
        inventoryAlertSocketPublisher
                .publishToHead(
                        savedAlert
                );

        return Optional.of(
                savedAlert
        );
    }

    /*
     * 특정 재고의 활성 알람 조회
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
         * 1. 해당 지점에 모달 데이터 전송
         */
        inventoryAlertSocketPublisher
                .publishToStore(
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
}