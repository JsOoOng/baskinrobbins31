package com.kiosk.customer.product.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTS")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;       // 상품 고유 식별자 (PK) [cite: 9]

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;      // 소속 카테고리 식별자 (FK) [cite: 9]

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;     // 상품명 (예: 파인트, 싱글레귤러) [cite: 9]

    @Column(columnDefinition = "TEXT")
    private String description;     // 상품 상세 설명 [cite: 9, 10]

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;       // 상품 기본 가격 [cite: 10]

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private Double discountRate;     // 상품 자체 할인율 (%) [cite: 10]

    @Builder.Default
    @Column(name = "is_display", nullable = false)
    private Boolean isDisplay = true; // 키오스크 노출 여부 [cite: 10, 11]

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 상품 등록 일시 [cite: 11]
}