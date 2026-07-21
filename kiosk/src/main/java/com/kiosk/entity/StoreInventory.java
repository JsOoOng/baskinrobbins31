package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
     * 지점의 실제 재고 수량
     *
     * 상품 최초 등록 시 0으로 시작합니다.
     */
    @Column(
            name = "current_stock",
            nullable = false
    )
    @Builder.Default
    private Integer currentStock = 0;

    /*
     * INSERT 또는 UPDATE 시 Hibernate가 시간을 관리합니다.
     */
    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    /*
     * Builder를 사용하지 않는 경우에도
     * currentStock이 null로 저장되지 않게 방지합니다.
     */
    @PrePersist
    protected void initializeStock() {

        if (currentStock == null) {
            currentStock = 0;
        }
    }

    /*
     * 재고 감소
     */
    public void decreaseStock(Integer amount) {

        if (amount == null || amount <= 0) {
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
     *
     * 이후 발주 완료 처리에서 사용할 수 있습니다.
     */
    public void increaseStock(Integer amount) {

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException(
                    "입고 수량은 1 이상이어야 합니다."
            );
        }

        if (currentStock == null) {
            currentStock = 0;
        }

        this.currentStock += amount;
    }
}