package com.kiosk.customer.product.controller;

import com.kiosk.customer.product.dto.ProductListResponse;
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

    // 특정 카테고리의 상품 리스트 조회 (지점별 품절 여부 포함)
    @GetMapping("/stores/{storeId}/categories/{categoryId}/products")
    public ResponseEntity<List<ProductListResponse>> getProductsByCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId) {
        
        return ResponseEntity.ok(productService.getProductsByCategory(storeId, categoryId));
    }
}