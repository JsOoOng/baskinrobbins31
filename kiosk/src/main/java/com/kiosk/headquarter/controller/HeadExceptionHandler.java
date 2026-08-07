package com.kiosk.headquarter.controller;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kiosk.headquarter.dto.common.HeadApiResponse;

import lombok.extern.slf4j.Slf4j;

/** 본사 API 예외가 내부 구현 내용을 노출하지 않도록 안전한 응답으로 변환합니다. */
@RestControllerAdvice(basePackages = "com.kiosk.headquarter.controller")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class HeadExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<HeadApiResponse<Void>> handleBadRequest(Exception e) {
        log.warn("본사 API 잘못된 요청", e);
        return response(HttpStatus.BAD_REQUEST, "요청값이 올바르지 않습니다.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<HeadApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("본사 API JSON 파싱 실패", e);
        return response(HttpStatus.BAD_REQUEST, "요청 본문 형식이 올바르지 않습니다.");
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<HeadApiResponse<Void>> handleConflict(IllegalStateException e) {
        log.warn("본사 API 상태 충돌", e);
        return response(HttpStatus.CONFLICT, "현재 상태에서는 요청을 처리할 수 없습니다.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<HeadApiResponse<Void>> handleUnauthorized(AuthenticationException e) {
        log.warn("본사 API 인증 실패", e);
        return response(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HeadApiResponse<Void>> handleForbidden(AccessDeniedException e) {
        log.warn("본사 API 권한 부족", e);
        return response(HttpStatus.FORBIDDEN, "요청을 수행할 권한이 없습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HeadApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("본사 API 처리되지 않은 서버 오류", e);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 요청을 처리하지 못했습니다.");
    }

    private ResponseEntity<HeadApiResponse<Void>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(HeadApiResponse.fail(message));
    }
}
