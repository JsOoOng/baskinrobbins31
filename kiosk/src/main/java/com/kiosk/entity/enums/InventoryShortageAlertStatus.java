package com.kiosk.entity.enums;

/*
 * 지점 재고 부족 알람 처리 상태
 */
public enum InventoryShortageAlertStatus {

    /*
     * 시스템에서 재고 부족을 감지한 상태
     *
     * 본사 재고 현황 화면에 표시됩니다.
     */
    DETECTED,

    /*
     * 본사 관리자가 지점에
     * 부족 알람을 전송한 상태
     */
    SENT,

    /*
     * 지점 관리자가
     * 알람 모달의 확인 버튼을 누른 상태
     */
    CONFIRMED,

    /*
     * 지점 관리자가
     * 재고 신청을 완료한 상태
     */
    REQUESTED,

    /*
     * 재고 부족이 해소된 상태
     */
    RESOLVED
}