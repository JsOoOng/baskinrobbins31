package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.RestockStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * [코드 흐름 안내] RestockRequest
 *
 * <p>역할: 재입고·발주 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 RESTOCK_REQUESTS 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 store_inventory_id, store_flavor_id, admin_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
     * 제품 재고 발주
     *
     * 상품 발주일 경우 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "store_inventory_id"
    )
    private StoreInventory storeInventory;



    /*
     * 맛 재고 발주
     *
     * 맛 발주일 경우 사용
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "store_flavor_id"
    )
    private StoreFlavor storeFlavor;



    /*
     * 요청 수량
     */
    @Column(
            name = "request_quantity",
            nullable = false
    )
    private Integer requestQuantity;



    /*
     * 발주 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private RestockStatus status =
            RestockStatus.WAITING;



    /*
     * 처리 관리자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "admin_id"
    )
    private HeadquarterAdmin admin;



    /*
     * 요청 시간
     */
    @CreationTimestamp
    @Column(
            name = "requested_at",
            updatable = false
    )
    private LocalDateTime requestedAt;



    
    
    

    /*
     * 제품 재고 ID 반환
     *
     * 제품 발주가 아니면 null
     */
    public Integer getStoreInventoryId() {

        if (storeInventory == null) {
            return null;
        }

        return storeInventory.getId();
    }



    /*
     * 맛 재고 ID 반환
     *
     * 맛 발주가 아니면 null
     */
    public Integer getStoreFlavorId() {

        if (storeFlavor == null) {
            return null;
        }

        return storeFlavor.getId();
    }




    /*
     * 관리자 지정
     *
     * 지점 발주 생성 시 사용
     */
    public void assignAdmin(
            HeadquarterAdmin admin
    ) {

        this.admin = admin;
    }




    /*
     * 승인 처리
     */
    public void approve(
            HeadquarterAdmin admin
    ) {

        if (
            this.status != RestockStatus.WAITING
        ) {

            throw new IllegalStateException(
                    "대기 중인 발주 요청만 승인할 수 있습니다."
            );
        }


        this.status =
                RestockStatus.APPROVED;


        this.admin =
                admin;
    }




    /*
     * 배송 시작
     */
    public void startShipping(
            HeadquarterAdmin admin
    ) {

        if (
            this.status != RestockStatus.APPROVED
        ) {

            throw new IllegalStateException(
                    "승인된 발주 요청만 배송 처리할 수 있습니다."
            );
        }


        this.status =
                RestockStatus.SHIPPING;


        this.admin =
                admin;
    }




    /*
     * 배송 완료
     */
    public void complete(
            HeadquarterAdmin admin
    ) {

        if (
            this.status != RestockStatus.SHIPPING
        ) {

            throw new IllegalStateException(
                    "배송 중인 발주 요청만 완료 처리할 수 있습니다."
            );
        }


        this.status =
                RestockStatus.COMPLETED;


        this.admin =
                admin;
    }

    /*
     * 지점 취소
     */
    public void cancel() {
        if (this.status != RestockStatus.WAITING) {
            throw new IllegalStateException("대기 중인 발주 요청만 취소할 수 있습니다.");
        }
        this.status = RestockStatus.CANCELED;
    }

    /*
     * 반려 처리
     */
    public void reject(HeadquarterAdmin admin) {
        if (this.status != RestockStatus.WAITING) {
            throw new IllegalStateException("대기 중인 발주 요청만 반려할 수 있습니다.");
        }
        this.status = RestockStatus.REJECTED;
        this.admin = admin;
    }

}