package com.kiosk.headquarter.inventory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoRestockScheduler {

    private final AutoRestockService
            autoRestockService;

    /*
     * 테스트용
     *
     * 작업 완료 후 10초 뒤 다시 실행합니다.
     */
    
    /*
    @Scheduled(fixedDelay = 10000)
    public void runAutoRestockTest() {

        log.info(
                "전체 자동 재고 보충 테스트 실행"
        );

        try {
            /*
             * DAILY / BOTH
             
            autoRestockService
                    .processDailyRestock();

            /*
             * THRESHOLD / BOTH
             
            autoRestockService
                    .processThresholdRestockSweep();

        } catch (Exception exception) {
            log.error(
                    "전체 자동 재고 보충 테스트 실패",
                    exception
            );
        }
    }
    */

    /*
     * 운영용 예시
     *
     * 테스트 완료 후 위 fixedDelay를 주석 처리하고
     * 아래 스케줄을 사용합니다.
     */
    
    @Scheduled(
            cron = "0 0 11 * * *",
            zone = "Asia/Seoul"
    )
    public void runDailyRestock() {

        autoRestockService
                .processDailyRestock();

        autoRestockService
                .processThresholdRestockSweep();
    }

}