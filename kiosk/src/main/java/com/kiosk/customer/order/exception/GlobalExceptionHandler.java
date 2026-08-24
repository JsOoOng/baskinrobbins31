package com.kiosk.customer.order.exception;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.kiosk.branch.auth.exception.LoginAttemptException;

import lombok.extern.slf4j.Slf4j;

/** 모든 API에서 발생한 예외를 안전한 고정 메시지와 올바른 HTTP 상태로 변환합니다. */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception e) {
        log.warn("잘못된 요청", e);
        return error(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다.");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException e) {
        log.warn("현재 상태에서 처리할 수 없는 요청", e);
        return error(HttpStatus.CONFLICT, "현재 상태에서는 요청을 처리할 수 없습니다.");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        log.warn("요청한 리소스를 찾지 못함", e);
        return error(HttpStatus.NOT_FOUND, "요청한 정보를 찾을 수 없습니다.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(AuthenticationException e) {
        log.warn("인증되지 않은 요청", e);
        return error(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(AccessDeniedException e) {
        log.warn("권한이 부족한 요청", e);
        return error(HttpStatus.FORBIDDEN, "요청을 수행할 권한이 없습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
        log.error("처리되지 않은 서버 오류", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 요청을 처리하지 못했습니다.");
    }
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(
            ResponseStatusException e
    ) {
        log.warn("HTTP 상태 오류: {}", e.getReason());

        return error(
                HttpStatus.valueOf(e.getStatusCode().value()),
                e.getReason() != null
                        ? e.getReason()
                        : "요청을 처리할 수 없습니다."
        );
    }

    @ExceptionHandler(LoginAttemptException.class)
    public ResponseEntity<Map<String, Object>> handleLoginAttempt(
            LoginAttemptException e
    ) {

        if (e.isBlocked()) {

            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "code", "LOGIN_BLOCKED",
                            "message", "로그인 시도 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.",
                            "failedCount", e.getFailedAttempts(),
                            "maxAttempts", e.getMaxAttempts(),
                            "blocked", true
                    ));
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "code", "LOGIN_FAILED",
                        "message", "아이디 또는 비밀번호가 올바르지 않습니다.",
                        "failedCount", e.getFailedAttempts(),
                        "maxAttempts", e.getMaxAttempts(),
                        "blocked", false
                ));
    }
    
    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "code", status.name(),
                "message", message
        ));
    }
}
