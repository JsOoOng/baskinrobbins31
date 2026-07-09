package com.kiosk.customer.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/kiosk")
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductService productService;

    // 3. 상품 상세 진입 시: 옵션 정보(사이즈, 토핑 등) 및 선택 가능한 맛 목록을 한 번에 반환
    @GetMapping("/stores/{storeId}/products/{productId}/detail")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            @PathVariable Long storeId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductDetail(storeId, productId));
    }
}