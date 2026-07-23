package com.kiosk.entity;

import com.kiosk.entity.enums.DeliveryStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] Delivery
 *
 * <p>역할: 배송 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 DELIVERIES 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 restock_request_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "DELIVERIES")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Delivery {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Integer id;



    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "restock_request_id",
            nullable = false
    )
    private RestockRequest restockRequest;



    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.READY;



    /**
     * 배송 상태 변경
     */
    public void changeStatus(DeliveryStatus status) {

        if(this.status == DeliveryStatus.COMPLETED){
            throw new IllegalStateException(
                "완료된 배송은 변경할 수 없습니다."
            );
        }

        this.status = status;

    }

}