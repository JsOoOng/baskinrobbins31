package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CATEGORIES")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer id;

    @Column(
            name = "category_name",
            length = 50,
            nullable = false
    )
    private String categoryName;

    @Column(
            name = "display_order",
            nullable = false
    )
    @Builder.Default
    private Integer displayOrder = 0;

    /*
     * true  : 고객 화면에 노출
     * false : 고객 화면에서 숨김
     */
    @Column(
            name = "active",
            nullable = false
    )
    @Builder.Default
    private Boolean active = true;

    public void updateCategory(
            String categoryName,
            Integer displayOrder,
            Boolean active
    ) {
        this.categoryName = categoryName;
        this.displayOrder = displayOrder;

        if (active != null) {
            this.active = active;
        }
    }

    public void changeActive(Boolean active) {

        if (active == null) {
            return;
        }

        this.active = active;
    }
}