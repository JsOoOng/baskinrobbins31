package com.kiosk.customer.order.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private int orderId;
    private String paymentMethod; // "CARD" 또는 "E_PAY"
}