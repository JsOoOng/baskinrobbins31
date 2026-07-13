package com.kiosk.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationConfig {

    @Bean
    public CommandLineRunner migrateDatabase(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE payments MODIFY COLUMN payment_method ENUM('CARD', 'CASH', 'E_PAY', 'COUPON', 'TOSS')");
                System.out.println("=========================================================");
                System.out.println("데이터베이스 마이그레이션 완료: payment_method ENUM에 'TOSS' 추가됨");
                System.out.println("=========================================================");
            } catch (Exception e) {
                System.out.println("데이터베이스 마이그레이션 실패 (이미 적용되었거나 권한 문제일 수 있습니다): " + e.getMessage());
            }
        };
    }
}
