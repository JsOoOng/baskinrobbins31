package com.kiosk.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.kiosk.entity.enums.DeliveryStatus;
import com.kiosk.entity.enums.RestockStatus;

class DeliveryCancellationTest {

    /*
     * 쉬운주석: 배송 취소 시 두 엔티티가 각각 취소와 반려 상태가 되는지 확인한다.
     */
    @Test
    void cancelDeliveryAlsoRejectsRestockRequest() {
        RestockRequest request = RestockRequest.builder()
                .requestQuantity(1)
                .status(RestockStatus.APPROVED)
                .build();
        Delivery delivery = Delivery.builder()
                .restockRequest(request)
                .status(DeliveryStatus.READY)
                .build();

        delivery.cancel("본사 요청");
        request.rejectDelivery(null, "본사 요청");

        assertEquals(DeliveryStatus.CANCELED, delivery.getStatus());
        assertEquals("본사 요청", delivery.getCancelReason());
        assertEquals(RestockStatus.REJECTED, request.getStatus());
        assertEquals("본사 요청", request.getRejectionReason());
    }
}
