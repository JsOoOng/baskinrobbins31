package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * [코드 흐름 안내] StoreProduct
 *
 * <p>역할: 지점 판매 상품 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 STORE_PRODUCTS 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 store_id, product_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean manualSoldOut = false;
    
    public void updateSoldOut(Boolean isSoldOut) {
        this.isSoldOut = isSoldOut;
    }
    
    public void changeManualSoldOut(Boolean manualSoldOut){

        this.manualSoldOut = manualSoldOut;

    }

    public void deleteStoreProduct() {
        this.isDeleted = true;
    }
    
    public void changeSoldOut(Boolean isSoldOut) {
    	this.isSoldOut = isSoldOut;
    }

    /*
     * 쉬운주석: 본사 수정 모달에서 바꾼 지점별 판매 가격을 엔티티에 반영한다.
     */
    public void changeStoreProductPrice(Integer storeProductPrice) {
        this.storeProductPrice = storeProductPrice;
    }
}
