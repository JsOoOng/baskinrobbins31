package com.kiosk.entity.enums;

/**
 * [코드 흐름 안내] AutoRestockMode
 *
 * <p>역할: 재입고·발주에서 허용하는 상태나 유형 값을 한곳에 모아 둔 Enum이다.</p>
 * <p>호출 흐름: Entity/DTO/Service가 같은 값을 공유하며, 문자열 값은 DB와 JSON에도 사용될 수 있다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public enum AutoRestockMode {

    /*
     * 판매 후 최소 재고 이하가 되면 즉시 보충
     */
    THRESHOLD,

    /*
     * 매일 정해진 시각에 목표 재고까지 보충
     */
    DAILY,

    /*
     * 임계 재고 방식과 정기 방식 모두 사용
     */
    BOTH
}