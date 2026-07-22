package com.kiosk.headquarter.controller;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.kiosk.headquarter.dto.common.HeadApiResponse;

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