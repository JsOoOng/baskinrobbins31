package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.product.HeadProductCreateRequestDTO;
import com.kiosk.headquarter.dto.product.HeadProductResponseDTO;
import com.kiosk.headquarter.service.HeadProductService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadProductController
 *
 * <p>역할: 본사 관리의 상품·메뉴 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> HeadProductService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class HeadProductController {

    private final HeadProductService headProductService;

    // 본사 상품 등록
    /**
     * [요청 흐름] POST /head/products
     * 프론트 요청을 받아 createProduct() 메서드가 입력을 받고 HeadProductService 호출 후 결과를 응답한다.
     */
    @PostMapping("/head/products")
    public HeadProductResponseDTO createProduct(
            @RequestBody HeadProductCreateRequestDTO requestDTO) {

        return headProductService.createProduct(requestDTO);
    }

    // 본사 상품 목록 조회
    /**
     * [요청 흐름] GET /head/products
     * 프론트 요청을 받아 getProductList() 메서드가 입력을 받고 HeadProductService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/products")
    public List<HeadProductResponseDTO> getProductList() {
        return headProductService.getProductList();
    }

    // 본사 상품 상세 조회
    /**
     * [요청 흐름] GET /head/products/{productId}
     * 프론트 요청을 받아 getProductDetail() 메서드가 입력을 받고 HeadProductService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/products/{productId}")
    public HeadProductResponseDTO getProductDetail(
            @PathVariable Integer productId) {

        return headProductService.getProductDetail(productId);
    }

    // 본사 상품 수정
    /**
     * [요청 흐름] PUT /head/products/{productId}
     * 프론트 요청을 받아 updateProduct() 메서드가 입력을 받고 HeadProductService 호출 후 결과를 응답한다.
     */
    @PutMapping("/head/products/{productId}")
    public HeadProductResponseDTO updateProduct(
            @PathVariable Integer productId,
            @RequestBody HeadProductCreateRequestDTO requestDTO) {

        return headProductService.updateProduct(productId, requestDTO);
    }

    // 본사 상품 삭제 처리
    /**
     * [요청 흐름] DELETE /head/products/{productId}
     * 프론트 요청을 받아 deleteProduct() 메서드가 입력을 받고 HeadProductService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/head/products/{productId}")
    public String deleteProduct(
            @PathVariable Integer productId) {

        headProductService.deleteProduct(productId);
        return "본사 상품 삭제 성공";
    }
}