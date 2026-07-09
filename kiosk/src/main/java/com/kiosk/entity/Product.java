package com.kiosk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {
	// 본사 상품 원본 메뉴
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "product_name", length = 100, nullable = false)
    private String productName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ZERO;

    @Column(name = "is_display")
    @Builder.Default
    private Boolean isDisplay = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public void updateProduct(
        Category category,
        String productName,
        String description,
        Integer basePrice,
        BigDecimal discountRate,
        Boolean isDisplay) {

    this.category = category;
    this.productName = productName;
    this.description = description;
    this.basePrice = basePrice;
    this.discountRate = discountRate;
    this.isDisplay = isDisplay;
    }

    public void hideProduct() {
    this.isDisplay = false;
    }
}