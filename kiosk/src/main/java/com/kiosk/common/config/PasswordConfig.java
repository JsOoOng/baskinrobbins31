package com.kiosk.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * [코드 흐름 안내] PasswordConfig
 *
 * <p>역할: 공통 기반 기능에서 사용하는 Spring 설정과 Bean 연결을 담당한다.</p>
 * <p>호출 흐름: 애플리케이션 시작 -> 이 설정 로딩 -> Bean/필터/보안 규칙 등록 -> 요청 처리에 적용된다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Configuration
public class PasswordConfig {

    @Bean
    /**
     * [메서드 흐름] passwordEncoder
     * 애플리케이션 시작 과정에서 호출되어 필요한 Bean이나 프레임워크 설정을 등록한다.
     */
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
