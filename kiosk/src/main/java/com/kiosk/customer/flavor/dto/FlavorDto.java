package com.kiosk.customer.flavor.dto; // 본인 패키지에 맞게 수정

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

/**
 * [코드 흐름 안내] FlavorDto
 *
 * <p>역할: 아이스크림 맛 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Getter
@Setter
@NoArgsConstructor
public class FlavorDto {
    private Integer flavorId;
    private String flavorName;
}