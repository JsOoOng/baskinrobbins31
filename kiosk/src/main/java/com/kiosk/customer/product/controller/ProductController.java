package com.kiosk.customer.product.controller;

import com.kiosk.customer.product.dto.ProductListResponse;
import com.kiosk.customer.category.dto.CategoryResponse; // 🌟 이거 꼭 임포트 해줘!
import com.kiosk.customer.product.service.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kiosk")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 🌟 1. 전체 카테고리 목록 조회 (이 창구를 새로 추가!)
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    // 2. 특정 카테고리의 상품 리스트 조회 (기존 코드 그대로 유지)
    @GetMapping("/stores/{storeId}/categories/{categoryId}/products")
    public ResponseEntity<List<ProductListResponse>> getProductsByCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId) {
        
        return ResponseEntity.ok(productService.getProductsByCategory(storeId, categoryId));
    }   
}