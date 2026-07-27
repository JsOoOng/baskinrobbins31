package com.kiosk.headquarter.inventory;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * [코드 흐름 안내] AutoRestockScheduler
 *
 * <p>역할: 본사 관리의 재입고·발주 처리를 보조하는 class다.</p>
 * <p>호출 흐름: 호출하는 클래스에서 필요한 값을 받아 현재 메서드의 결과를 다음 계층으로 전달한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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