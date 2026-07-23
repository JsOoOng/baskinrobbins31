package com.kiosk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * [코드 흐름 안내] WebSocketConfig
 *
 * <p>역할: 애플리케이션 설정에서 사용하는 Spring 설정과 Bean 연결을 담당한다.</p>
 * <p>호출 흐름: 애플리케이션 시작 -> 이 설정 로딩 -> Bean/필터/보안 규칙 등록 -> 요청 처리에 적용된다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Override
    /**
     * [메서드 흐름] configureMessageBroker
     * 애플리케이션 시작 과정에서 호출되어 필요한 Bean이나 프레임워크 설정을 등록한다.
     */
    public void configureMessageBroker(
            MessageBrokerRegistry registry) {

        // 클라이언트가 구독하는 주소
        registry.enableSimpleBroker("/topic");

    }


    @Override
    /**
     * [메서드 흐름] registerStompEndpoints
     * 애플리케이션 시작 과정에서 호출되어 필요한 Bean이나 프레임워크 설정을 등록한다.
     */
    public void registerStompEndpoints(
            StompEndpointRegistry registry) {

        // Vue가 접속할 WebSocket 주소
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

    }
}