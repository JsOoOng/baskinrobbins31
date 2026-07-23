package com.kiosk.entity.enums;

public enum NotificationType {

    /*
     * 재고
     */
    LOW_STOCK,
    AUTO_RESTOCK_SUCCESS,
    AUTO_RESTOCK_FAILED,
    RESTOCK_REQUEST,

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