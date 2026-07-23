package com.kiosk.common.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.kiosk.headquarter.dto.restock.HeadRestockRequestSocketMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * [코드 흐름 안내] RestockRequestSocketPublisher
 *
 * <p>역할: 공통 기반 기능의 재입고·발주 처리를 보조하는 class다.</p>
 * <p>호출 흐름: 호출하는 클래스에서 필요한 값을 받아 현재 메서드의 결과를 다음 계층으로 전달한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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