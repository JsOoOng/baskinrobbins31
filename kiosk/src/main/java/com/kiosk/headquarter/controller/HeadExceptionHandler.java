package com.kiosk.headquarter.controller;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.kiosk.headquarter.dto.common.HeadApiResponse;

/**
 * [코드 흐름 안내] HeadExceptionHandler
 *
 * <p>역할: 본사 관리의 본사 공통 예외 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestControllerAdvice(basePackages = "com.kiosk.headquarter.controller")
public class HeadExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<HeadApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        e.printStackTrace();
        return ResponseEntity.ok(HeadApiResponse.fail(e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<HeadApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HeadApiResponse.fail("JSON 파싱 오류: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HeadApiResponse<Void>> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.ok(HeadApiResponse.fail("오류 내용: " + e.getMessage()));
    }
}