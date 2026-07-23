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