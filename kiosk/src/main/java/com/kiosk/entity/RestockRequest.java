package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.RestockStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RESTOCK_REQUESTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RestockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer id;


    /*
     * 제품 재고 요청
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_inventory_id")
    private StoreInventory storeInventory;


    /*
     * 맛 재고 요청
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_flavor_id")
    private StoreFlavor storeFlavor;


    /*
     * 요청 수량
     */
    @Column(name = "request_quantity", nullable = false)
    private Integer requestQuantity;


    /*
     * 발주 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private RestockStatus status = RestockStatus.WAITING;


    /*
     * 처리 관리자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private HeadquarterAdmin admin;


    /*
     * 요청 시간
     */
    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;



    /*
     * 제품 재고 ID 반환
     * 값이 없으면 0 반환
     */
    public Integer getStoreInventoryId() {

        if (storeInventory == null) {
            return 0;
        }

        return storeInventory.getId();
    }



    /*
     * 맛 재고 ID 반환
     * 값이 없으면 0 반환
     */
    public Integer getStoreFlavorId() {

        if (storeFlavor == null) {
            return 0;
        }

        return storeFlavor.getId();
    }



    /*
     * 승인
     */
    public void approve(HeadquarterAdmin admin) {

        if (this.status != RestockStatus.WAITING) {
            throw new IllegalStateException(
                "대기 중인 발주 요청만 승인할 수 있습니다."
            );
        }

        this.status = RestockStatus.APPROVED;
        this.admin = admin;
    }



    /*
     * 배송 시작
     */
    public void startShipping(HeadquarterAdmin admin) {

        if (this.status != RestockStatus.APPROVED) {
            throw new IllegalStateException(
                "승인된 발주 요청만 배송 처리할 수 있습니다."
            );
        }

        this.status = RestockStatus.SHIPPING;
        this.admin = admin;
    }



    /*
     * 배송 완료
     */
    public void complete(HeadquarterAdmin admin) {

        if (this.status != RestockStatus.SHIPPING) {
            throw new IllegalStateException(
                "배송 중인 발주 요청만 완료 처리할 수 있습니다."
            );
        }

        this.status = RestockStatus.COMPLETED;
        this.admin = admin;
    }
}