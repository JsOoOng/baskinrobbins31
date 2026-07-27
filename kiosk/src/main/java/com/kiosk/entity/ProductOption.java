package com.kiosk.entity;

import com.kiosk.entity.enums.OptionType;
import jakarta.persistence.*;
import lombok.*;

/**
 * [코드 흐름 안내] ProductOption
 *
 * <p>역할: 상품 옵션 데이터를 나타내는 JPA Entity다.</p>
 * <p>호출 흐름: Repository가 현재 코드의 PRODUCT_OPTIONS 테이블 매핑을 통해 이 객체를 저장·조회한다. 관계 키는 product_id를 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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
    
    public void updateOption(
            OptionType optionType,
            String optionName,
            Integer extraPrice,
            Integer maxFlavorCount) {

        this.optionType = optionType;
        this.optionName = optionName;
        this.extraPrice = extraPrice;
        this.maxFlavorCount = maxFlavorCount;
    }
}