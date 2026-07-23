package com.kiosk.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.kiosk.entity.enums.InventoryShortageAlertStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "inventory_shortage_alerts"
)
@Getter
@Builder
@NoArgsConstructor(
        access = AccessLevel.PROTECTED
)
@AllArgsConstructor
public class InventoryShortageAlert {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "alert_id")
    private Integer id;

    /*
     * 부족이 발생한 지점 재고
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "store_inventory_id",
            nullable = false
    )
    private StoreInventory storeInventory;

    /*
     * 알람 대상 지점
     *
     * StoreInventory에서도 지점을 찾을 수 있지만,
     * 본사·지점별 알람 조회를 단순하게 하기 위해
     * 별도로 저장합니다.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "store_id",
            nullable = false
    )
    private Store store;

    /*
     * 부족한 재고 품목
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "item_id",
            nullable = false
    )
    private InventoryItem item;

    /*
     * 부족 감지 당시 현재 재고
     */
    @Column(
            name = "current_stock_snapshot",
            nullable = false
    )
    private Integer currentStockSnapshot;

    /*
     * 부족 감지 당시 최소 재고
     */
    @Column(
            name = "min_stock_snapshot",
            nullable = false
    )
    private Integer minStockSnapshot;

    /*
     * 부족 수량
     *
     * minStockSnapshot
     * - currentStockSnapshot
     */
    @Column(
            name = "shortage_quantity",
            nullable = false
    )
    private Integer shortageQuantity;

    /*
     * 알람 처리 상태
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "alert_status",
            nullable = false,
            length = 20
    )
    private InventoryShortageAlertStatus
            alertStatus =
            InventoryShortageAlertStatus
                    .DETECTED;

    /*
     * 알람을 지점에 보낸
     * 본사 관리자 번호
     */
    @Column(name = "sent_by_admin_id")
    private Integer sentByAdminId;

    /*
     * 지점에서 생성한
     * 재고 신청 번호
     */
    @Column(name = "restock_request_id")
    private Integer restockRequestId;

    /*
     * 부족 최초 감지 시각
     */
    @CreationTimestamp
    @Column(
            name = "detected_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime detectedAt;

    /*
     * 지점 알람 전송 시각
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /*
     * 지점 관리자 확인 시각
     */
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    /*
     * 재고 신청 완료 시각
     */
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    /*
     * 부족 해소 시각
     */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /*
     * 마지막 변경 시각
     */
    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    /*
     * DB에서 자동 생성하는 중복 방지 키
     *
     * Java에서 직접 입력하거나
     * 수정하지 않습니다.
     */
    @Column(
            name = "active_alert_key",
            insertable = false,
            updatable = false
    )
    private String activeAlertKey;

    /*
     * 신규 부족 알람 생성
     */
    public static InventoryShortageAlert detect(
            StoreInventory inventory
    ) {

        validateInventory(
                inventory
        );

        int shortageQuantity =
                inventory
                        .calculateShortageQuantity();

        if (shortageQuantity <= 0) {
            throw new IllegalArgumentException(
                    "현재 재고는 부족 상태가 아닙니다."
            );
        }

        return InventoryShortageAlert
                .builder()
                .storeInventory(
                        inventory
                )
                .store(
                        inventory.getStore()
                )
                .item(
                        inventory.getItem()
                )
                .currentStockSnapshot(
                        inventory
                                .getCurrentStock()
                )
                .minStockSnapshot(
                        inventory
                                .getMinStock()
                )
                .shortageQuantity(
                        shortageQuantity
                )
                .alertStatus(
                        InventoryShortageAlertStatus
                                .DETECTED
                )
                .build();
    }

    /*
     * 재고가 계속 감소했을 때
     * 기존 활성 알람의 부족 수량 갱신
     */
    public void refreshShortage(
            StoreInventory inventory
    ) {

        validateInventory(
                inventory
        );

        if (
                storeInventory == null ||
                storeInventory.getId() == null ||
                !Objects.equals(
                        storeInventory.getId(),
                        inventory.getId()
                )
        ) {
            throw new IllegalArgumentException(
                    "다른 재고의 알람은 갱신할 수 없습니다."
            );
        }

        int currentShortageQuantity =
                inventory
                        .calculateShortageQuantity();

        /*
         * 더 이상 부족하지 않다면
         * 알람을 해결 상태로 변경합니다.
         */
        if (currentShortageQuantity <= 0) {
            resolve();

            return;
        }

        this.currentStockSnapshot =
                inventory.getCurrentStock();

        this.minStockSnapshot =
                inventory.getMinStock();

        this.shortageQuantity =
                currentShortageQuantity;
    }

    /*
     * 본사 관리자가 지점에
     * 알람을 전송합니다.
     */
    public void markSent(
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

        if (
                alertStatus !=
                InventoryShortageAlertStatus
                        .DETECTED
        ) {
            throw new IllegalStateException(
                    "부족 감지 상태의 알람만 전송할 수 있습니다."
            );
        }

        this.alertStatus =
                InventoryShortageAlertStatus
                        .SENT;

        this.sentByAdminId =
                adminId;

        this.sentAt =
                LocalDateTime.now();
    }

    /*
     * 지점 관리자가 부족 알림을 확인합니다.
     *
     * DETECTED:
     * 시스템 자체 알림을 확인한 경우
     *
     * SENT:
     * 본사가 따로 보낸 알림을 확인한 경우
     */
    public void confirm() {

        boolean confirmable =
                alertStatus ==
                        InventoryShortageAlertStatus
                                .DETECTED
                ||
                alertStatus ==
                        InventoryShortageAlertStatus
                                .SENT;

        if (!confirmable) {
            throw new IllegalStateException(
                    "확인할 수 있는 재고 부족 알림이 아닙니다."
            );
        }

        this.alertStatus =
                InventoryShortageAlertStatus
                        .CONFIRMED;

        this.confirmedAt =
                LocalDateTime.now();
    }

    /*
     * 지점에서 재고 신청을 완료합니다.
     */
    public void markRequested(
            Integer restockRequestId
    ) {

        if (
                restockRequestId == null ||
                restockRequestId <= 0
        ) {
            throw new IllegalArgumentException(
                    "재고 신청 번호가 올바르지 않습니다."
            );
        }

        if (
                alertStatus !=
                InventoryShortageAlertStatus
                        .CONFIRMED
        ) {
            throw new IllegalStateException(
                    "확인된 알람만 재고 신청과 연결할 수 있습니다."
            );
        }

        this.alertStatus =
                InventoryShortageAlertStatus
                        .REQUESTED;

        this.restockRequestId =
                restockRequestId;

        this.requestedAt =
                LocalDateTime.now();
    }

    /*
     * 재고 부족 해소
     */
    public void resolve() {

        if (
                alertStatus ==
                InventoryShortageAlertStatus
                        .RESOLVED
        ) {
            return;
        }

        this.alertStatus =
                InventoryShortageAlertStatus
                        .RESOLVED;

        this.resolvedAt =
                LocalDateTime.now();
    }

    /*
     * 활성 알람 여부
     */
    public boolean isActiveAlert() {

        return alertStatus !=
                InventoryShortageAlertStatus
                        .RESOLVED;
    }

    /*
     * 신규 생성·갱신에 필요한
     * 재고 정보 검증
     */
    private static void validateInventory(
            StoreInventory inventory
    ) {

        if (inventory == null) {
            throw new IllegalArgumentException(
                    "지점 재고 정보가 없습니다."
            );
        }

        if (inventory.getId() == null) {
            throw new IllegalArgumentException(
                    "지점 재고 번호가 없습니다."
            );
        }

        if (inventory.getStore() == null) {
            throw new IllegalArgumentException(
                    "지점 정보가 없습니다."
            );
        }

        if (inventory.getItem() == null) {
            throw new IllegalArgumentException(
                    "재고 품목 정보가 없습니다."
            );
        }

        if (inventory.getCurrentStock() == null) {
            throw new IllegalArgumentException(
                    "현재 재고 정보가 없습니다."
            );
        }

        if (inventory.getMinStock() == null) {
            throw new IllegalArgumentException(
                    "최소 재고 정보가 없습니다."
            );
        }
    }
}