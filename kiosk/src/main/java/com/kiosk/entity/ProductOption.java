package com.kiosk.entity;

import com.kiosk.entity.enums.OptionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PRODUCT_OPTIONS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false)
    private OptionType optionType;

    @Column(name = "option_name", length = 50, nullable = false)
    private String optionName;

    @Column(name = "extra_price")
    @Builder.Default
    private Integer extraPrice = 0;

    @Column(name = "max_flavor_count")
    @Builder.Default
    private Integer maxFlavorCount = 0;
}