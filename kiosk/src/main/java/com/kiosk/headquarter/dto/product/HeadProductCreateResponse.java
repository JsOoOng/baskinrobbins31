package com.kiosk.headquarter.dto.product;

import java.util.List;

public class HeadProductCreateResponse {
	// 상품 응답용 DTO
    private Integer productId;
    private Integer categoryId;
    private String categoryName;
    private String productName;
    private String description;
    private Integer basePrice;
    private Integer discountRate;
    private Boolean isDisplay;

    private List<HeadProductOptionResponse> options;
    private List<Integer> storeIds;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Integer basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Integer discountRate) {
        this.discountRate = discountRate;
    }

    public Boolean getIsDisplay() {
        return isDisplay;
    }

    public void setIsDisplay(Boolean isDisplay) {
        this.isDisplay = isDisplay;
    }

    public List<HeadProductOptionResponse> getOptions() {
        return options;
    }

    public void setOptions(List<HeadProductOptionResponse> options) {
        this.options = options;
    }

    public List<Integer> getStoreIds() {
        return storeIds;
    }

    public void setStoreIds(List<Integer> storeIds) {
        this.storeIds = storeIds;
    }
}