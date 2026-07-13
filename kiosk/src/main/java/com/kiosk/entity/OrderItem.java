package com.kiosk.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ORDER_ITEMS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;
    
    //추가한 부분입니다. 혹시 수정이 있으시 다시 반영
    @OneToMany(
    	    mappedBy = "orderItem",
    	    fetch = FetchType.LAZY,
    	    cascade = CascadeType.ALL
    	)
    	@Builder.Default
    	private List<OrderItemFlavor> orderItemFlavors = new ArrayList<>();
    
    @OneToMany(
            mappedBy = "orderItem",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @Builder.Default
    private List<OrderItemOption> orderItemOptions = new ArrayList<>();
}