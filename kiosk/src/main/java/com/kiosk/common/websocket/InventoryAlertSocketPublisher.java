package com.kiosk.common.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.kiosk.common.websocket.dto.InventoryShortageSocketMessage;
import com.kiosk.entity.InventoryShortageAlert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryAlertSocketPublisher {

    /*
     * 본사 재고 부족 알림 채널
     */
    private static final String
            HEAD_INVENTORY_SHORTAGE_TOPIC =
            "/topic/head/inventory-shortages";

    /*
     * 지점 재고 부족 알림 채널
     *
     * 시스템 자체 알림과
     * 본사 수동 알림 모두 같은 채널을 사용하고,
     * source 값으로 구분합니다.
     */
    private static final String
            STORE_TOPIC_PREFIX =
            "/topic/stores/";

    private static final String
            STORE_TOPIC_SUFFIX =
            "/inventory-shortages";

    private final SimpMessagingTemplate
            messagingTemplate;

    /*
     * 본사에 자동 부족 알림 전송
     */
    public void publishToHead(
            InventoryShortageAlert alert
    ) {

        InventoryShortageSocketMessage message =
                InventoryShortageSocketMessage
                        .forHead(alert);

        send(
                HEAD_INVENTORY_SHORTAGE_TOPIC,
                message,
                "본사 재고 부족"
        );
    }

    /*
     * 지점에 시스템 자체 부족 알림 전송
     *
     * 재고가 처음 부족해진 순간
     * 자동으로 실행합니다.
     */
    public void publishSelfAlertToStore(
            InventoryShortageAlert alert
    ) {

        String destination =
                createStoreDestination(
                        alert
                );

        InventoryShortageSocketMessage message =
                InventoryShortageSocketMessage
                        .forStoreSelf(alert);

        send(
                destination,
                message,
                "지점 자체 재고 부족"
        );
    }

    /*
     * 본사 관리자가 버튼을 눌렀을 때
     * 지점에 별도 알림 전송
     */
    public void publishHeadReminderToStore(
            InventoryShortageAlert alert
    ) {

        String destination =
                createStoreDestination(
                        alert
                );

        InventoryShortageSocketMessage message =
                InventoryShortageSocketMessage
                        .forStoreHeadReminder(alert);

        send(
                destination,
                message,
                "본사 발신 재고 부족"
        );
    }

    private String createStoreDestination(
            InventoryShortageAlert alert
    ) {

        if (
                alert == null ||
                alert.getStore() == null ||
                alert.getStore().getId() == null
        ) {
            throw new IllegalArgumentException(
                    "알림 대상 지점 정보가 없습니다."
            );
        }

        return STORE_TOPIC_PREFIX
                + alert.getStore().getId()
                + STORE_TOPIC_SUFFIX;
    }

    private void send(
            String destination,
            InventoryShortageSocketMessage message,
            String logTitle
    ) {

        try {
            messagingTemplate.convertAndSend(
                    destination,
                    message
            );

            log.info(
                    "{} WebSocket 전송 완료: "
                    + "destination={}, alertId={}, "
                    + "storeId={}, source={}",

                    logTitle,
                    destination,
                    message.getAlertId(),
                    message.getStoreId(),
                    message.getSource()
            );

        } catch (Exception exception) {

            /*
             * WebSocket 전송 실패로
             * 주문과 재고 차감 트랜잭션까지
             * 취소하지 않습니다.
             */
            log.error(
                    "{} WebSocket 전송 실패: "
                    + "destination={}, alertId={}",

                    logTitle,
                    destination,
                    message.getAlertId(),
                    exception
            );
        }
    }
}