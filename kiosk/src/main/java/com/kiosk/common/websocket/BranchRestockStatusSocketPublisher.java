package com.kiosk.common.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.kiosk.branch.status.dto.BranchRestockStatusSocketMessage;
import com.kiosk.entity.RestockRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BranchRestockStatusSocketPublisher {

    private final SimpMessagingTemplate
            messagingTemplate;

    /*
     * 승인·반려 트랜잭션이 정상적으로 커밋된 후
     * 해당 지점에 알림을 전송합니다.
     */
    public void publishAfterCommit(
            RestockRequest restockRequest
    ) {

        /*
         * 트랜잭션이 끝난 뒤 Lazy Entity에 접근하지 않도록
         * DTO를 미리 생성합니다.
         */
        BranchRestockStatusSocketMessage message =
                BranchRestockStatusSocketMessage
                        .from(
                                restockRequest
                        );

        if (
                message.getStoreId() == null ||
                message.getStoreId() <= 0
        ) {
            throw new IllegalStateException(
                    "알림을 받을 지점 번호가 없습니다."
            );
        }

        /*
         * 현재 트랜잭션이 존재하면
         * DB 커밋 성공 후 전송합니다.
         */
        if (
                TransactionSynchronizationManager
                        .isActualTransactionActive()
                &&
                TransactionSynchronizationManager
                        .isSynchronizationActive()
        ) {

            TransactionSynchronizationManager
                    .registerSynchronization(
                            new TransactionSynchronization() {

                                @Override
                                public void afterCommit() {

                                    send(
                                            message
                                    );
                                }
                            }
                    );

            return;
        }

        /*
         * 트랜잭션 밖에서 호출된 경우
         * 즉시 전송합니다.
         */
        send(
                message
        );
    }

    private void send(
            BranchRestockStatusSocketMessage message
    ) {

        String destination =
                "/topic/stores/"
                + message.getStoreId()
                + "/restock-requests";

        try {

            messagingTemplate
                    .convertAndSend(
                            destination,
                            message
                    );

            log.info(
                    "지점 재고 신청 상태 WebSocket 전송 완료: "
                    + "storeId={}, requestId={}, status={}",

                    message.getStoreId(),
                    message.getRequestId(),
                    message.getStatus()
            );

        } catch (Exception exception) {

            /*
             * DB 처리는 이미 정상 커밋된 상태이므로
             * WebSocket 실패가 승인·반려 결과를 취소하지 않습니다.
             */
            log.error(
                    "지점 재고 신청 상태 WebSocket 전송 실패: "
                    + "storeId={}, requestId={}, status={}",

                    message.getStoreId(),
                    message.getRequestId(),
                    message.getStatus(),
                    exception
            );
        }
    }
}