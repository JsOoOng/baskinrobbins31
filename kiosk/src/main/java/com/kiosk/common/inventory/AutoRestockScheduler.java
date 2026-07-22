package com.kiosk.common.inventory;

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
     * 매일 오전 12시에 실행합니다.
     *
     * 초  분 시  일  월  요일
     * 0  0  10  *  *  *
     */
    
    /*
    @Scheduled(
            cron = "0 0 12 * * *",
            zone = "Asia/Seoul"
    )
    public void runDailyRestock() {

        log.info(
                "정기 자동 재고 보충 작업을 시작합니다."
        );

        try {
            autoRestockService
                    .processDailyRestock();

        } catch (Exception exception) {
            log.error(
                    "정기 자동 재고 보충 작업에 실패했습니다.",
                    exception
            );
        }
    }
	*/

    @Scheduled(fixedDelay = 10000)
    public void runDailyRestock() {

        log.info(
                "자동 재고 보충 테스트 실행"
        );

        autoRestockService
                .processDailyRestock();
    }

}