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

    @Column(name = "discount_rate")
    @Builder.Default
    private Integer discountRate = 0;

    @Column(name = "margin_rate")
    @Builder.Default
    private Integer marginRate = 65;

    @Column(name = "regular_price")
    private Integer regularPrice;

    @Column(name = "final_price")
    private Integer finalPrice;

    @Column(name = "is_display")
    @Builder.Default
    private Boolean isDisplay = true;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    public void calculatePrices() {
        if (this.basePrice != null) {
            int currentMarginRate = (this.marginRate != null) ? this.marginRate : 65;
            int currentDiscountRate = (this.discountRate != null) ? this.discountRate : 0;

            // 정가 = 반올림(원가 * (1 + 마진율/100)) 후 100원 단위
            double calcRegular = this.basePrice * (1.0 + (currentMarginRate / 100.0));
            this.regularPrice = (int) (Math.round(calcRegular / 100.0) * 100);

            // 할인 후 가격 = 반올림(정가 * (1 - 할인율/100)) 후 100원 단위
            double calcFinal = this.regularPrice * (1.0 - (currentDiscountRate / 100.0));
            this.finalPrice = (int) (Math.round(calcFinal / 100.0) * 100);
        }
    }

    // 사진 수정 메서드에 imageUrl 파라미터가 추가됨!
    public void updateProduct(
        Category category,
        String productName,
        String description,
        Integer basePrice,
        Integer discountRate,
        Integer marginRate,
        Boolean isDisplay,
        String imageUrl) {

        this.category = category;
        this.productName = productName;
        this.description = description;
        this.basePrice = basePrice;
        if (discountRate != null) this.discountRate = discountRate;
        if (marginRate != null) this.marginRate = marginRate;
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