package com.kiosk.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.kiosk.common.config.JwtTokenStore;
import com.kiosk.common.config.JwtUtil;

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

    private final JwtUtil jwtUtil;
    private final JwtTokenStore jwtTokenStore;

    public WebSocketConfig(JwtUtil jwtUtil, JwtTokenStore jwtTokenStore) {
        this.jwtUtil = jwtUtil;
        this.jwtTokenStore = jwtTokenStore;
    }

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
                .setAllowedOrigins(
                        "http://localhost:5173",
                        "https://baskinrobbins31.store",
                        "https://www.baskinrobbins31.store"
                );

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                /*
                 * CONNECT에서 설정한 Principal은 원본 STOMP accessor에 기록해야
                 * WebSocket 세션에 보존되고 이후 SUBSCRIBE 프레임에도 전달됩니다.
                 * wrap(message)는 별도 accessor를 만들기 때문에 setUser() 결과가
                 * 실제 inbound message에 반영되지 않을 수 있습니다.
                 */
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(
                                message,
                                StompHeaderAccessor.class
                        );

                if (accessor == null) {
                    return message;
                }

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    authenticateConnect(accessor);
                }

                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    authorizeSubscription(accessor);
                }

                return message;
            }
        });
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new AccessDeniedException("WebSocket 인증이 필요합니다. (쿠키 누락)");
        }
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof Authentication authentication)) {
            throw new AccessDeniedException("WebSocket 인증이 필요합니다.");
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            throw new AccessDeniedException("WebSocket 구독 주소가 필요합니다.");
        }

        boolean isHeadTopic = destination.startsWith("/topic/head/");
        boolean isBranchTopic = destination.startsWith("/topic/store/")
                || destination.startsWith("/topic/stores/");

        if (isHeadTopic && !hasAuthority(authentication, "TYPE_HEAD")) {
            throw new AccessDeniedException("본사 WebSocket 구독 권한이 없습니다.");
        }

        if (isBranchTopic && !hasAuthority(authentication, "TYPE_BRANCH")) {
            throw new AccessDeniedException("지점 WebSocket 구독 권한이 없습니다.");
        }
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }
}
