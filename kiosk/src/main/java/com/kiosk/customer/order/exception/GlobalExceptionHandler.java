package com.kiosk.customer.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.HashMap;

// @RestControllerAdvice: 모든 컨트롤러의 예외를 여기서 가로채겠다!
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 재고 부족, 잘못된 요청 등(400 Bad Request)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, RuntimeException.class})
    public ResponseEntity<Map<String, String>> handleBusinessException(RuntimeException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage()); // 서비스에서 던진 메시지를 JSON으로 변환
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}