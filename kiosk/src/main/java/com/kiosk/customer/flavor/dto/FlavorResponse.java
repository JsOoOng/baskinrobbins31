package com.kiosk.customer.flavor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] FlavorResponse
 *
 * <p>역할: 아이스크림 맛 처리 결과를 프론트에 반환하기 위한 응답 DTO다.</p>
 * <p>호출 흐름: Entity/조회 결과 -> Service 변환 -> 이 DTO -> Controller JSON 응답 순서로 사용한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FlavorResponse {
    private Integer flavorId;     // 맛 식별자 [cite: 14]
    private String flavorName;   // 맛 이름 (예: 민트초코, 엄마는 외계인) [cite: 14]
    private Boolean isSoldOut;   // 현재 지점 내 품절 여부 (STORE_FLAVORS 테이블 연동) [cite: 17, 18]
    private String imageUrl;     // 아이스크림 이미지 URL
}