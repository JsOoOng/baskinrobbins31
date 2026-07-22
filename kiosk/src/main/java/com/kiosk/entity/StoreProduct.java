package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STORE_PRODUCTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StoreProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_product_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "store_product_price", nullable = false)
    private Integer storeProductPrice;

    @Column(name = "is_sold_out")
    @Builder.Default
    private Boolean isSoldOut = false;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;
    
    public void updateSoldOut(Boolean isSoldOut) {
        this.isSoldOut = isSoldOut;
    }

    public void deleteStoreProduct() {
        this.isDeleted = true;
    }
    
    public void changeSoldOut(Boolean isSoldOut) {
    	this.isSoldOut = isSoldOut;
    }
}