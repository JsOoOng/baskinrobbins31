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

@RestController
@RequiredArgsConstructor
public class HeadProductController {

    private final HeadProductService headProductService;

    // 본사 상품 등록
    @PostMapping("/head/products")
    public HeadProductResponseDTO createProduct(
            @RequestBody HeadProductCreateRequestDTO requestDTO) {

        return headProductService.createProduct(requestDTO);
    }

    // 본사 상품 목록 조회
    @GetMapping("/head/products")
    public List<HeadProductResponseDTO> getProductList() {
        return headProductService.getProductList();
    }

    // 본사 상품 상세 조회
    @GetMapping("/head/products/{productId}")
    public HeadProductResponseDTO getProductDetail(
            @PathVariable Integer productId) {

        return headProductService.getProductDetail(productId);
    }

    // 본사 상품 수정
    @PutMapping("/head/products/{productId}")
    public HeadProductResponseDTO updateProduct(
            @PathVariable Integer productId,
            @RequestBody HeadProductCreateRequestDTO requestDTO) {

        return headProductService.updateProduct(productId, requestDTO);
    }

    // 본사 상품 삭제 처리
    @DeleteMapping("/head/products/{productId}")
    public String deleteProduct(
            @PathVariable Integer productId) {

        headProductService.deleteProduct(productId);
        return "본사 상품 삭제 성공";
    }
}