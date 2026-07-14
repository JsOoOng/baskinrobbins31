package com.kiosk.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // 💡 수정 메서드에 imageUrl 파라미터도 추가!
    public void updateProduct(
        Category category,
        String productName,
        String description,
        Integer basePrice,
        BigDecimal discountRate,
        Boolean isDisplay,
        String imageUrl) {

        this.category = category;
        this.productName = productName;
        this.description = description;
        this.basePrice = basePrice;
        this.discountRate = discountRate;
        this.isDisplay = isDisplay;
        this.imageUrl = imageUrl;
    }

    public void hideProduct() {
        this.isDisplay = false;
    }
    
    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<InventoryItem> inventoryItems = new ArrayList<>();
}