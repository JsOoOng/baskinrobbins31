package com.kiosk.customer.product.controller;

import com.kiosk.customer.product.dto.OrderCreateRequest;
import com.kiosk.customer.product.dto.ProductListResponse;
import com.kiosk.customer.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kiosk")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/stores/{storeId}/categories/{categoryId}/products")
    public ResponseEntity<List<ProductListResponse>> getProductsByCategory(
            @PathVariable Long storeId,
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(storeId, categoryId));
    }

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody OrderCreateRequest request) {
        productService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("주문이 정상적으로 접수되었습니다.");
    }
}