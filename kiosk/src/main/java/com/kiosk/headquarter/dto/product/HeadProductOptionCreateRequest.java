package com.kiosk.headquarter.dto.product;

public class HeadProductOptionCreateRequest {
	// 상품 옵션 요청용 DTO
    private String optionType;
    private String optionName;
    private Integer extraPrice;
    private Integer maxFlavorCount;

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