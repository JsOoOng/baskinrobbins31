package com.kiosk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {


    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry) {

        // 클라이언트가 구독하는 주소
        registry.enableSimpleBroker("/topic");

    }


    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry) {

        // Vue가 접속할 WebSocket 주소
        registry.addEndpoint("/ws")
                .setAllowedOrigins(
                        "http://localhost:5173"
                );

    }
}