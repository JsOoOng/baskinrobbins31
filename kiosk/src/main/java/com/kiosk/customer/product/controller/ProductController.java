package com.kiosk.customer.product.controller;

import com.kiosk.customer.product.dto.ProductListResponse;
import com.kiosk.customer.category.dto.CategoryResponse; // 🌟 이거 꼭 임포트 해줘!
import com.kiosk.customer.product.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * [코드 흐름 안내] ProductController
 *
 * <p>역할: 고객 키오스크의 상품·메뉴 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/v1/kiosk) -> ProductService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/v1/kiosk")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 🌟 1. 전체 카테고리 목록 조회 (이 창구를 새로 추가!)
    /**
     * [요청 흐름] GET /api/v1/kiosk/categories
     * 프론트 요청을 받아 getAllCategories() 메서드가 입력을 받고 ProductService 호출 후 결과를 응답한다.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    // 2. 특정 카테고리의 상품 리스트 조회 (기존 코드 그대로 유지)
    /**
     * [요청 흐름] GET /api/v1/kiosk/stores/{storeId}/categories/{categoryId}/products
     * 프론트 요청을 받아 getProductsByCategory() 메서드가 입력을 받고 ProductService 호출 후 결과를 응답한다.
     */
    @GetMapping("/stores/{storeId}/categories/{categoryId}/products")
    public ResponseEntity<List<ProductListResponse>> getProductsByCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId) {
        
        return ResponseEntity.ok(productService.getProductsByCategory(storeId, categoryId));
    }   
}