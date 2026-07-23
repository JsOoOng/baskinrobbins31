package com.kiosk.customer.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * [코드 흐름 안내] ProductCreateRequest
 *
 * <p>역할: 상품·메뉴 요청 JSON 값을 Controller와 Service 사이로 전달한다.</p>
 * <p>호출 흐름: 프론트 JSON -> Controller의 @RequestBody -> 이 DTO -> Service 검증 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@NoArgsConstructor
public class ProductCreateRequest {
    private Integer categoryId;      // 소속 카테고리 ID (예: 아이스크림 카테고리 ID)
    private String productName;     // 상품명 (예: 파인트, 쿼터)
    private String description;     // 상품 상세 설명
    private Integer basePrice;      // 상품 기본 가격
    
    private List<OptionCreateDto> options; // 상품 등록 시 함께 추가할 옵션 목록

    @Getter
    @NoArgsConstructor
    public static class OptionCreateDto {
        private String optionType;    // 옵션 종류 (CONTAINER, SIZE, TOPPING 등)
        private String optionName;    // 옵션명 (예: 파인트 컵, 스푼 3개)
        private Integer extraPrice;   // 옵션 추가 금액
        private Integer maxFlavorCount; // 선택 가능한 최대 맛 개수 (사이즈 옵션인 경우)
    }
}