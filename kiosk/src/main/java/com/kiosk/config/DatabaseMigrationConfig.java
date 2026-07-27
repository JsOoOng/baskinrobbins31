package com.kiosk.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * [코드 흐름 안내] DatabaseMigrationConfig
 *
 * <p>역할: 애플리케이션 설정에서 사용하는 Spring 설정과 Bean 연결을 담당한다.</p>
 * <p>호출 흐름: 애플리케이션 시작 -> 이 설정 로딩 -> Bean/필터/보안 규칙 등록 -> 요청 처리에 적용된다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Configuration
public class DatabaseMigrationConfig {

    @Bean
    /**
     * [메서드 흐름] migrateDatabase
     * 애플리케이션 시작 과정에서 호출되어 필요한 Bean이나 프레임워크 설정을 등록한다.
     */
    public CommandLineRunner migrateDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE payments MODIFY COLUMN payment_method ENUM('CARD', 'CASH', 'E_PAY', 'COUPON', 'TOSS')");
                System.out.println("데이터베이스 마이그레이션 완료: payment_method ENUM에 'TOSS' 추가됨");
            } catch (Exception e) {
                System.out.println("payment_method 마이그레이션 실패: " + e.getMessage());
            }

            /*
             * 쉬운주석: Java의 배송 상태에 CANCELED가 추가되어도 기존 MySQL ENUM은 자동으로
             * 늘어나지 않을 수 있으므로, 서버 시작 때 DB가 같은 다섯 상태를 받도록 맞춘다.
             */
            try {
                jdbcTemplate.execute(
                        "ALTER TABLE deliveries MODIFY COLUMN delivery_status "
                        + "ENUM('READY', 'STARTED', 'IN_PROGRESS', 'COMPLETED', 'CANCELED') NOT NULL"
                );
                System.out.println("데이터베이스 마이그레이션 완료: delivery_status에 'CANCELED' 추가됨");
            } catch (Exception e) {
                System.out.println("delivery_status 마이그레이션 실패: " + e.getMessage());
            }
        };
    }
}
