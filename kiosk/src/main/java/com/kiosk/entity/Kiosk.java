package com.kiosk.entity;

import com.kiosk.entity.enums.KioskStatus;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * [코드 흐름 안내] Kiosk
 *
 * <p>역할: 키오스크 기기 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 KIOSKS 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 store_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Entity
@Table(name = "KIOSKS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Kiosk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kiosk_id")
    private Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;


    @Column(name = "kiosk_number", nullable = false)
    private Integer kioskNumber;


    @Column(name = "device_serial", length = 100, unique = true)
    private String deviceSerial;


    @Enumerated(EnumType.STRING)
    @Column(name = "kiosk_status", nullable = false)
    @Builder.Default
    private KioskStatus kioskStatus = KioskStatus.ONLINE;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    public void changeStatus(KioskStatus status){

        this.kioskStatus = status;

    }

}