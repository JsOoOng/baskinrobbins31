package com.kiosk.headquarter.dto.product;

public class HeadProductOptionResponse {
	// 옵션 응답용 DTO
    private Integer optionId;
    private Integer productId;
    private String optionType;
    private String optionName;
    private Integer extraPrice;
    private Integer maxFlavorCount;

    public Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public Integer getExtraPrice() {
        return extraPrice;
    }

    public void setExtraPrice(Integer extraPrice) {
        this.extraPrice = extraPrice;
    }

    public Integer getMaxFlavorCount() {
        return maxFlavorCount;
    }

    public void setMaxFlavorCount(Integer maxFlavorCount) {
        this.maxFlavorCount = maxFlavorCount;
    }
}