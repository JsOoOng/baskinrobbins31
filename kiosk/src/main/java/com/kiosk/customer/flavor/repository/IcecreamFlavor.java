package com.kiosk.customer.flavor.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ICECREAM_FLAVORS")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IcecreamFlavor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flavor_id")
    private Integer flavorId;     // 맛 고유 식별자 (PK)

    @Column(name = "flavor_name", nullable = false, length = 50)
    private String flavorName;    // 맛 이름 (예: 엄마는 외계인, 민트초코)

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // 본사 차원의 맛 운영/단종 여부
}