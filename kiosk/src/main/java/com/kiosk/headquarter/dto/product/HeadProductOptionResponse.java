package com.kiosk.headquarter.dto.product;

/**
 * [코드 흐름 안내] HeadProductOptionResponse
 *
 * <p>역할: 상품 옵션 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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