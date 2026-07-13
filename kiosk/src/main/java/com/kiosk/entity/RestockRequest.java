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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private InventoryItem item;

    @Column(name = "request_quantity", nullable = false)
    private Integer requestQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private RestockStatus status = RestockStatus.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private HeadquarterAdmin admin;

    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    public void approve(HeadquarterAdmin admin) {
        if (this.status != RestockStatus.WAITING) {
            throw new IllegalStateException("대기 중인 발주 요청만 승인할 수 있습니다.");
        }

        this.status = RestockStatus.APPROVED;
        this.admin = admin;
    }

    public void startShipping(HeadquarterAdmin admin) {
        if (this.status != RestockStatus.APPROVED) {
            throw new IllegalStateException("승인된 발주 요청만 배송 처리할 수 있습니다.");
        }

        this.status = RestockStatus.SHIPPING;
        this.admin = admin;
    }

    public void complete(HeadquarterAdmin admin) {
        if (this.status != RestockStatus.SHIPPING) {
            throw new IllegalStateException("배송 중인 발주 요청만 완료 처리할 수 있습니다.");
        }

        this.status = RestockStatus.COMPLETED;
        this.admin = admin;
    }
}