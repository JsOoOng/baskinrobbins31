package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [코드 흐름 안내] OrderItemFlavor
 *
 * <p>역할: 주문 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 ORDER_ITEM_FLAVORS 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 order_item_id, flavor_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "ORDER_ITEM_FLAVORS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItemFlavor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_flavor_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flavor_id", nullable = false)
    private IcecreamFlavor flavor;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}