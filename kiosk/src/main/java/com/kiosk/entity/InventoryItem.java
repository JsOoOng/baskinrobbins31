package com.kiosk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "INVENTORY_ITEMS")
@Getter
@NoArgsConstructor(
        access = AccessLevel.PROTECTED
)
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "item_id")
    private Integer id;

    /*
     * 어떤 상품의 재고 품목인지 연결
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false
    )
    private Product product;

    /*
     * 재고 화면에 표시할 품목명
     */
    @Column(
            name = "item_name",
            length = 100,
            nullable = false
    )
    private String itemName;

    @Column(
            name = "unit",
            length = 20,
            nullable = false
    )
    private String unit;

    @Column(
            name = "unit_price",
            nullable = false
    )
    @Builder.Default
    private Integer unitPrice = 0;
}