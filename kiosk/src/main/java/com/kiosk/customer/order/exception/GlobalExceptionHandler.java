package com.kiosk.customer.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
import java.util.HashMap;

/**
 * [코드 흐름 안내] GlobalExceptionHandler
 *
 * <p>역할: 고객 키오스크의 주문 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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