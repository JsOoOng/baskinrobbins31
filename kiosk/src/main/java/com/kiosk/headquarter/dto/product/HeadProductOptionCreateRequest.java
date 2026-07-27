package com.kiosk.headquarter.dto.product;

/**
 * [코드 흐름 안내] HeadProductOptionCreateRequest
 *
 * <p>역할: 상품 옵션 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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