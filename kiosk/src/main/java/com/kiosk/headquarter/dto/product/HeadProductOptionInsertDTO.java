package com.kiosk.headquarter.dto.product;

/**
 * [코드 흐름 안내] HeadProductOptionInsertDTO
 *
 * <p>역할: 상품 옵션 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public class HeadProductOptionInsertDTO {
	// 옵션 INSERT용 DTO
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