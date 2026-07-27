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

/**
 * [코드 흐름 안내] InventoryItem
 *
 * <p>역할: 재고 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 INVENTORY_ITEMS 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 product_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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