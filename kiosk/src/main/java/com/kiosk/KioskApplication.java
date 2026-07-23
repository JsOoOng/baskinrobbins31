package com.kiosk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * [코드 흐름 안내] KioskApplication
 *
 * <p>역할: Spring Boot 백엔드 애플리케이션을 시작하는 최상위 진입점이다.</p>
 * <p>호출 흐름: main() 실행 -> Spring 설정·Bean·Controller 탐색 -> 내장 서버 시작 -> HTTP 요청 대기 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@EnableScheduling
@SpringBootApplication(
        scanBasePackages = "com.kiosk"
)
public class KioskApplication {

    public static void main(
            String[] args
    ) {

        SpringApplication.run(
                KioskApplication.class,
                args
        );
    }
}