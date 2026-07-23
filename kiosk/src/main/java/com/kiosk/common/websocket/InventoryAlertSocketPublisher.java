package com.kiosk.common.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.kiosk.entity.InventoryShortageAlert;
import com.kiosk.headquarter.dto.inventoryalert.HeadInventoryShortageSocketResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryAlertSocketPublisher {

    /*
     * 본사 재고 현황 구독 주소
     */
    private static final String
            HEAD_INVENTORY_SHORTAGE_TOPIC =
            "/topic/head/inventory-shortages";

    /*
     * 지점별 재고 부족 알람 주소
     *
     * 예:
     * /topic/stores/1/inventory-shortages
     */
    private static final String
            STORE_INVENTORY_SHORTAGE_TOPIC_PREFIX =
            "/topic/stores/";

    private static final String
            STORE_INVENTORY_SHORTAGE_TOPIC_SUFFIX =
            "/inventory-shortages";

    private final SimpMessagingTemplate
            messagingTemplate;

    /*
     * 본사 재고 현황 화면에 전송
     */
    public void publishToHead(
            InventoryShortageAlert alert
    ) {

        if (alert == null) {
            log.warn(
                    "본사 WebSocket 전송 실패: 알람 정보 없음"
            );

            return;
        }

        try {
            HeadInventoryShortageSocketResponse response =
                    HeadInventoryShortageSocketResponse
                            .from(alert);

            messagingTemplate.convertAndSend(
                    HEAD_INVENTORY_SHORTAGE_TOPIC,
                    response
            );

            log.info(
                    "본사 재고 부족 전송 완료: "
                            + "alertId={}, storeId={}, "
                            + "itemId={}, status={}",

                    response.getAlertId(),
                    response.getStoreId(),
                    response.getItemId(),
                    response.getAlertStatus()
            );

        } catch (Exception exception) {

            /*
             * WebSocket 전송 실패로
             * 주문이나 재고 트랜잭션을
             * 롤백하지 않습니다.
             */
            log.error(
                    "본사 재고 부족 전송 실패: alertId={}",
                    alert.getId(),
                    exception
            );
        }
    }

    /*
     * 특정 지점 관리자에게
     * 재고 부족 모달 데이터를 전송
     */
    public void publishToStore(
            InventoryShortageAlert alert
    ) {

        if (alert == null) {
            log.warn(
                    "지점 WebSocket 전송 실패: 알람 정보 없음"
            );

            return;
        }

        if (
                alert.getStore() == null ||
                alert.getStore().getId() == null
        ) {
            log.warn(
                    "지점 WebSocket 전송 실패: "
                            + "지점 정보 없음, alertId={}",
                    alert.getId()
            );

            return;
        }

        Integer storeId =
                alert.getStore().getId();

        String destination =
                STORE_INVENTORY_SHORTAGE_TOPIC_PREFIX
                        + storeId
                        + STORE_INVENTORY_SHORTAGE_TOPIC_SUFFIX;

        try {
            HeadInventoryShortageSocketResponse response =
                    HeadInventoryShortageSocketResponse
                            .from(alert);

            messagingTemplate.convertAndSend(
                    destination,
                    response
            );

            log.info(
                    "지점 재고 부족 알람 전송 완료: "
                            + "destination={}, alertId={}, "
                            + "storeId={}, itemId={}, "
                            + "shortageQuantity={}",

                    destination,
                    response.getAlertId(),
                    response.getStoreId(),
                    response.getItemId(),
                    response.getShortageQuantity()
            );

        } catch (Exception exception) {

            log.error(
                    "지점 재고 부족 알람 전송 실패: "
                            + "alertId={}, storeId={}",

                    alert.getId(),
                    storeId,
                    exception
            );
        }
    }
}