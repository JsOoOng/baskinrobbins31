package com.kiosk.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.kiosk.entity.enums.StoreStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] Store
 *
 * <p>역할: 지점 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 STORES 테이블 매핑을 통해 이 객체를 저장·조회한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "STORES")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "store_id")
    private Integer id;

    @Column(
            name = "store_name",
            length = 100,
            nullable = false
    )
    private String storeName;

    @Column(
            name = "business_number",
            length = 20
    )
    private String businessNumber;

    @Column(
            name = "region",
            length = 50,
            nullable = false
    )
    private String region;

    @Column(
            name = "address",
            length = 255,
            nullable = false
    )
    private String address;

    @Column(
            name = "phone",
            length = 20
    )
    private String phone;

    @Column(
            name = "business_hours",
            length = 100
    )
    private String businessHours;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "store_status",
            nullable = false
    )
    @Builder.Default
    private StoreStatus storeStatus =
            StoreStatus.CLOSED;

    @CreationTimestamp
    @Column(
            name = "created_at",
            updatable = false
    )
    private LocalDateTime createdAt;

    /*
     * 지점 정보 수정
     */
    public void updateStore(
            String storeName,
            String businessNumber,
            String region,
            String address,
            String phone,
            String businessHours,
            StoreStatus storeStatus
    ) {
        this.storeName = storeName;
        this.businessNumber = businessNumber;
        this.region = region;
        this.address = address;
        this.phone = phone;
        this.businessHours = businessHours;
        this.storeStatus = storeStatus;
    }
}