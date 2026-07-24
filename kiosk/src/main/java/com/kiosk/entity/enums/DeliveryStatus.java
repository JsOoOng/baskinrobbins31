package com.kiosk.entity.enums;


/**
 * [코드 흐름 안내] DeliveryStatus
 *
 * <p>역할: 배송에서 허용하는 상태나 유형 값을 한곳에 모아 둔 Enum이다.</p>
 * <p>호출 흐름: Entity/DTO/Service가 같은 값을 공유하며, 문자열 값은 DB와 JSON에도 사용될 수 있다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public enum DeliveryStatus {

    READY,        // 배송 준비
    STARTED,      // 배송 시작
    IN_PROGRESS,  // 배송 중
    COMPLETED,    // 배송 완료
    CANCELED      // 배송 취소

}
