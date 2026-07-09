package com.kiosk.customer.product.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.product.dto.ProductCreateRequest;
import com.kiosk.customer.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;

    // 카테고리에 새로운 메뉴(상품) 추가 API
    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody ProductCreateRequest request) {
        productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("메뉴가 성공적으로 추가되었습니다.");
    }
}