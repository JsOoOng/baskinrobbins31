package com.kiosk.headquarter.inventory;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AutoRestockScheduler {

    /*
     * 기존 자동 재고 보충 스케줄러는 중지했습니다.
     *
     * STORE_INVENTORY:
     * 자동 보충 대신 재고 부족 알람을 생성합니다.
     *
     * STORE_FLAVORS:
     * 추후 같은 알람 방식으로 전환할 예정입니다.
     */
}