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

/**
 * [코드 흐름 안내] ProductAdminController
 *
 * <p>역할: 고객 키오스크의 상품·메뉴 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/v1/admin/products) -> ProductService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;

    // 카테고리에 새로운 메뉴(상품) 추가 API
    /**
     * [요청 흐름] POST /api/v1/admin/products
     * 프론트 요청을 받아 createProduct() 메서드가 입력을 받고 ProductService 호출 후 결과를 응답한다.
     */
    @PostMapping
    public ResponseEntity<String> createProduct(@RequestBody ProductCreateRequest request) {
        productService.addProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("메뉴가 성공적으로 추가되었습니다.");
    }
}