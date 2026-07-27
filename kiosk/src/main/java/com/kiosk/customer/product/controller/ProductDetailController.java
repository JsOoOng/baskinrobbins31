package com.kiosk.customer.product.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.customer.product.dto.ProductDetailResponse;
import com.kiosk.customer.product.service.ProductService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] ProductDetailController
 *
 * <p>역할: 고객 키오스크의 상품·메뉴 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/api/v1/kiosk) -> ProductService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/api/v1/kiosk")
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductService productService;

    // 3. 상품 상세 진입 시: 옵션 정보(사이즈, 토핑 등) 및 선택 가능한 맛 목록을 한 번에 반환
    /**
     * [요청 흐름] GET /api/v1/kiosk/stores/{storeId}/products/{productId}/detail
     * 프론트 요청을 받아 getProductDetail() 메서드가 입력을 받고 ProductService 호출 후 결과를 응답한다.
     */
    @GetMapping("/stores/{storeId}/products/{productId}/detail")
    public ResponseEntity<ProductDetailResponse> getProductDetail(
            @PathVariable Long storeId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductDetail(storeId, productId));
    }
}