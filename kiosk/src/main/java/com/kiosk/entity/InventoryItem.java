package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "INVENTORY_ITEMS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer id;

    @Column(name = "item_name", length = 100, nullable = false)
    private String itemName;

    @Column(name = "unit", length = 20, nullable = false)
    private String unit;

    @Column(name = "unit_price", nullable = false)
    @Builder.Default
    private Integer unitPrice = 0;
}