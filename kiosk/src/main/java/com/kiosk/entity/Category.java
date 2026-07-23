package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [코드 흐름 안내] Category
 *
 * <p>역할: 상품 카테고리 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 CATEGORIES 테이블 매핑을 통해 이 객체를 저장·조회한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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