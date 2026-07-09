package com.kiosk.entity.enums;

public enum OrderStatus {
	WAITING,   // <-- 이 값이 누락되어 에러가 났던 것입니다.
    PREPARING,
    COMPLETED,
    CANCELLED
}