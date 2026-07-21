package com.kiosk.entity.enums;

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