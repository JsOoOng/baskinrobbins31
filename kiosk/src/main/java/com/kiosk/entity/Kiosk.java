package com.kiosk.entity;

import com.kiosk.entity.enums.KioskStatus;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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