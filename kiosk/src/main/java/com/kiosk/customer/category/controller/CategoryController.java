package com.kiosk.customer.category.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.category.dto.CategoryResponse;
import com.kiosk.customer.category.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 전체 목록 조회 (노출 순서 정렬)
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        // 대문자 클래스가 아니라, 위에서 선언한 변수(categoryService)를 호출해야 합니다.
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}