package com.kiosk.entity.enums;

/**
 * [코드 흐름 안내] OptionType
 *
 * <p>역할: 상품 옵션 유형에서 허용하는 상태나 유형 값을 한곳에 모아 둔 Enum이다.</p>
 * <p>호출 흐름: Entity/DTO/Service가 같은 값을 공유하며, 문자열 값은 DB와 JSON에도 사용될 수 있다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public enum OptionType {

    CONTAINER,  // 컵, 콘, 포장 용기 등
    SIZE,       // 싱글, 더블, 파인트, 쿼터 등
    TOPPING,    // 초코칩, 시럽, 기타 토핑
    SPOON,      // 스푼 개수
    ETC         // 기타 옵션
}