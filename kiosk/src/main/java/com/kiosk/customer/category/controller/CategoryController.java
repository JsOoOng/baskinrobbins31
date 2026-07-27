package com.kiosk.customer.category.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.category.dto.CategoryResponse;
import com.kiosk.customer.category.service.CategoryService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] CategoryController
 *
 * <p>역할: 고객 키오스크의 상품 카테고리 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/v1/categories) -> CategoryService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 전체 목록 조회 (노출 순서 정렬)
    /**
     * [요청 흐름] GET /api/v1/categories
     * 프론트 요청을 받아 getCategories() 메서드가 입력을 받고 CategoryService 호출 후 결과를 응답한다.
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        // 대문자 클래스가 아니라, 위에서 선언한 변수(categoryService)를 호출해야 합니다.
        return ResponseEntity.ok(categoryService.getAllCategories());
    }
}