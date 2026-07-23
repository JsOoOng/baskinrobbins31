package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.kiosk.entity.enums.AutoRestockMode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import com.kiosk.entity.enums.AutoRestockMode;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] StoreInventory
 *
 * <p>역할: 지점 재고 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 STORE_INVENTORY 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 store_id, item_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "STORE_INVENTORY")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_inventory_id")
    private Integer id;

    /*
     * 재고를 보유하는 지점
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "store_id",
            nullable = false
    )
    private Store store;

    /*
     * 지점에서 관리할 재고 품목
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "item_id",
            nullable = false
    )
    private InventoryItem item;

    /*
     * 현재 재고
     */
    @Column(
            name = "current_stock",
            nullable = false
    )
    @Builder.Default
    private Integer currentStock = 0;

    /*
     * 임계 재고 기준
     *
     * THRESHOLD 또는 BOTH 방식에서
     * 현재 재고가 이 수량 이하가 되면 보충합니다.
     */
    @Column(
            name = "min_stock",
            nullable = false
    )
    @Builder.Default
    private Integer minStock = 10;

    /*
     * 자동 보충 후 목표 재고
     *
     * 예:
     * 현재 재고 8
     * 목표 재고 50
     * 자동 보충 수량 42
     */
    @Column(
            name = "target_stock",
            nullable = false
    )
    @Builder.Default
    private Integer targetStock = 50;

    /*
     * 자동 보충 기능 사용 여부
     */
    @Column(
            name = "auto_restock_enabled",
            nullable = false
    )
    @Builder.Default
    private Boolean autoRestockEnabled = true;

    /*
     * 자동 보충 실행 방식
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "restock_mode",
            length = 20,
            nullable = false
    )
    @Builder.Default
    private AutoRestockMode restockMode =
            AutoRestockMode.THRESHOLD;

    /*
     * 마지막 변경 시간
     */
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    /*
     * Builder를 사용하지 않았거나
     * 일부 값이 null인 경우 기본값 설정
     */
    @PrePersist
    protected void initializeInventory() {

        if (currentStock == null) {
            currentStock = 0;
        }

        if (minStock == null) {
            minStock = 10;
        }

        if (targetStock == null) {
            targetStock = 50;
        }

        if (autoRestockEnabled == null) {
            autoRestockEnabled = true;
        }

        if (restockMode == null) {
            restockMode =
                    AutoRestockMode.THRESHOLD;
        }
    }

    /*
     * 재고 감소
     */
    public void decreaseStock(Integer amount) {

        if (
                amount == null ||
                amount <= 0
        ) {
            throw new IllegalArgumentException(
                    "차감 수량은 1 이상이어야 합니다."
            );
        }

        if (
                currentStock == null ||
                currentStock < amount
        ) {
            throw new IllegalArgumentException(
                    "재고가 부족합니다."
            );
        }

        this.currentStock -= amount;
    }

    /*
     * 재고 증가
     */
    public void increaseStock(Integer amount) {

        if (
                amount == null ||
                amount <= 0
        ) {
            throw new IllegalArgumentException(
                    "입고 수량은 1 이상이어야 합니다."
            );
        }

        if (currentStock == null) {
            currentStock = 0;
        }

        this.currentStock += amount;
    }

    /*
     * 임계 재고 자동 보충 대상인지 확인
     */
    public boolean needsThresholdRestock() {

        if (
                !Boolean.TRUE.equals(
                        autoRestockEnabled
                )
        ) {
            return false;
        }

        if (
                restockMode !=
                        AutoRestockMode.THRESHOLD
                &&
                restockMode !=
                        AutoRestockMode.BOTH
        ) {
            return false;
        }

        if (
                currentStock == null ||
                minStock == null ||
                targetStock == null
        ) {
            return false;
        }

        return currentStock <= minStock
                && currentStock < targetStock;
    }

    /*
     * 정기 자동 보충 대상인지 확인
     */
    public boolean needsDailyRestock() {

        if (
                !Boolean.TRUE.equals(
                        autoRestockEnabled
                )
        ) {
            return false;
        }

        if (
                restockMode != AutoRestockMode.DAILY &&
                restockMode != AutoRestockMode.BOTH
        ) {
            return false;
        }

        if (
                currentStock == null ||
                targetStock == null
        ) {
            return false;
        }

        return currentStock < targetStock;
    }

    /*
     * 목표 재고까지 필요한 수량 계산
     */
    public Integer calculateRestockQuantity() {

        if (
                currentStock == null ||
                targetStock == null ||
                targetStock <= currentStock
        ) {
            return 0;
        }

        return targetStock - currentStock;
    }

    /*
     * 목표 재고까지 자동 보충
     *
     * 반환값:
     * 실제 보충한 수량
     */
    public Integer autoRestock() {

        if (
                currentStock == null ||
                targetStock == null ||
                currentStock >= targetStock
        ) {
            return 0;
        }

        Integer restockQuantity =
                targetStock - currentStock;

        currentStock = targetStock;

        return restockQuantity;
    }

    /*
     * 본사 관리 화면에서 자동 보충 설정을
     * 변경할 때 사용할 메서드
     */
    public void updateAutoRestockSetting(
            Integer minStock,
            Integer targetStock,
            Boolean autoRestockEnabled,
            AutoRestockMode restockMode
    ) {

        if (
                minStock == null ||
                minStock < 0
        ) {
            throw new IllegalArgumentException(
                    "최소 재고는 0 이상이어야 합니다."
            );
        }

        if (
                targetStock == null ||
                targetStock <= 0
        ) {
            throw new IllegalArgumentException(
                    "목표 재고는 1 이상이어야 합니다."
            );
        }

        if (targetStock < minStock) {
            throw new IllegalArgumentException(
                    "목표 재고는 최소 재고보다 작을 수 없습니다."
            );
        }

        if (restockMode == null) {
            throw new IllegalArgumentException(
                    "자동 보충 방식을 선택해주세요."
            );
        }

        this.minStock = minStock;
        this.targetStock = targetStock;
        this.autoRestockEnabled =
                Boolean.TRUE.equals(
                        autoRestockEnabled
                );
        this.restockMode = restockMode;
    }
    
    /*
     * 자동 재고 보충 설정 수정
     */
    public void updateAutoRestockSetting(
            Boolean autoRestockEnabled,
            AutoRestockMode restockMode,
            Integer minStock,
            Integer targetStock
    ) {

        this.autoRestockEnabled =
                autoRestockEnabled;

        this.restockMode =
                restockMode;

        this.minStock =
                minStock;

        this.targetStock =
                targetStock;
    }
    
    /*
     * 현재 재고가 최소 재고보다 적은지 확인
     */
    public boolean isLowStock() {

        if (
                currentStock == null ||
                minStock == null
        ) {
            return false;
        }

        return currentStock < minStock;
    }

    /*
     * 최소 재고 기준 부족 수량 계산
     *
     * 예:
     * currentStock = 6
     * minStock = 10
     * 결과 = 4
     */
    public int calculateShortageQuantity() {

        if (!isLowStock()) {
            return 0;
        }

        return minStock - currentStock;
    }
}