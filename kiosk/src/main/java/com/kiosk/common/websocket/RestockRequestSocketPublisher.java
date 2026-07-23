package com.kiosk.common.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.kiosk.headquarter.dto.restock.HeadRestockRequestSocketMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestockRequestSocketPublisher {

    /*
     * 본사 재고 신청 관리 화면 구독 주소
     */
    private static final String
            HEAD_RESTOCK_REQUEST_TOPIC =
            "/topic/head/restock-requests";

    private final SimpMessagingTemplate
            messagingTemplate;

    public void publishCreated(
            Integer requestId,
            Integer storeId,
            String storeName,
            String requestTargetType,
            String itemName,
            Integer requestQuantity
    ) {

        HeadRestockRequestSocketMessage message =
                HeadRestockRequestSocketMessage
                        .create(
                                requestId,
                                storeId,
                                storeName,
                                requestTargetType,
                                itemName,
                                requestQuantity
                        );

        try {
            messagingTemplate.convertAndSend(
                    HEAD_RESTOCK_REQUEST_TOPIC,
                    message
            );

            log.info(
                    "본사 재고 신청 WebSocket 전송 완료: "
                    + "requestId={}, storeId={}, type={}",

                    requestId,
                    storeId,
                    requestTargetType
            );

        } catch (Exception exception) {

            /*
             * WebSocket 전송 실패 때문에
             * 재고 신청 INSERT를 취소하지 않습니다.
             */
            log.error(
                    "본사 재고 신청 WebSocket 전송 실패: "
                    + "requestId={}, storeId={}",

                    requestId,
                    storeId,
                    exception
            );
        }
    }
}