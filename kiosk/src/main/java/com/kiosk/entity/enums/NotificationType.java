package com.kiosk.entity.enums;

/**
 * [코드 흐름 안내] NotificationType
 *
 * <p>역할: 알림에서 허용하는 상태나 유형 값을 한곳에 모아 둔 Enum이다.</p>
 * <p>호출 흐름: Entity/DTO/Service가 같은 값을 공유하며, 문자열 값은 DB와 JSON에도 사용될 수 있다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public enum NotificationType {

    /*
     * 재고
     */
    LOW_STOCK,
    AUTO_RESTOCK_SUCCESS,
    AUTO_RESTOCK_FAILED,
    
    
    RESTOCK_REQUEST,
    RESTOCK_REQUEST_CREATED,
    FLAVOR_RESTOCK_REQUEST_CREATED,

    /*
     * 쿠폰
     */
    COUPON_CREATED,
    COUPON_UPDATED,
    COUPON_ISSUED,
    COUPON_ISSUE_FAILED,
    COUPON_EXPIRING,

    /*
     * 이벤트
     */
    EVENT_CREATED,
    EVENT_UPDATED,
    EVENT_STARTED,
    EVENT_EXPIRING,
    EVENT_ENDED,

    /*
     * 배너
     */
    BANNER_CREATED,
    BANNER_UPDATED,
    BANNER_STARTED,
    BANNER_EXPIRING,
    BANNER_ENDED,

    /*
     * 공통 시스템
     */
    SYSTEM
}