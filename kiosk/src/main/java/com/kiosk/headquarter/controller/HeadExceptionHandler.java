package com.kiosk.headquarter.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kiosk.headquarter.dto.common.HeadApiResponse;

@RestControllerAdvice(basePackages = "com.example.demo.headquarter")
public class HeadExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public HeadApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        e.printStackTrace();
        return HeadApiResponse.fail(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public HeadApiResponse<Void> handleException(Exception e) {
        e.printStackTrace();
        return HeadApiResponse.fail("오류 내용: " + e.getMessage());
    }
}