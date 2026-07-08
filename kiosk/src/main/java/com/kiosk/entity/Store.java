package com.kiosk.entity;

import com.kiosk.entity.enums.StoreStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "STORES")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Integer id;

    @Column(name = "store_name", length = 100, nullable = false)
    private String storeName;

    @Column(name = "business_number", length = 20)
    private String businessNumber;

    @Column(name = "region", length = 50, nullable = false)
    private String region;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "business_hours", length = 100)
    private String businessHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_status", nullable = false)
    @Builder.Default
    private StoreStatus storeStatus = StoreStatus.CLOSED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}