package com.kiosk.customer.order.dto;

import lombok.Data;

@Data
public class TossConfirmRequest {
    private String paymentKey;
    private String orderId;
    private int amount;
    private int pointUsed;
}
