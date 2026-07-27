package com.kiosk.customer.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

import com.kiosk.customer.flavor.dto.FlavorResponse;

/**
 * [코드 흐름 안내] ProductDetailResponse
 *
 * <p>역할: 상품·메뉴 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {
    private Integer productId;      // 상품 식별자 [cite: 9]
    private String productName;    // 상품명 [cite: 9]
    private String description;    // 상품 상세 설명 [cite: 9]
    private Integer basePrice;     // 기본 가격
    private Integer finalPrice;    // 최종 가격
    
    // 묶여진 옵션 그룹 리스트 (사이즈 그룹, 토핑 그룹, 스푼 그룹 등)
    private List<OptionGroupDto> optionGroups; 
    
    // 선택 가능한 맛 목록 (지점 품절 여부 반영)
    private List<FlavorResponse> availableFlavors; 

    private List<ProductOptionDto> options;
}