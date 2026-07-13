package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.product.HeadProductOptionCreateRequestDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionResponseDTO;
import com.kiosk.headquarter.dto.product.HeadProductOptionUpdateRequestDTO;
import com.kiosk.headquarter.service.HeadProductOptionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HeadProductOptionController {

    private final HeadProductOptionService headProductOptionService;

    // 상품 옵션 등록
    @PostMapping("/head/products/{productId}/options")
    public String createProductOption(
            @PathVariable Integer productId,
            @RequestBody HeadProductOptionCreateRequestDTO requestDTO) {

        return headProductOptionService.createProductOption(productId, requestDTO);
    }

    // 상품 옵션 목록 조회
    @GetMapping("/head/products/{productId}/options")
    public List<HeadProductOptionResponseDTO> getProductOptionList(
            @PathVariable Integer productId) {

        return headProductOptionService.getProductOptionList(productId);
    }

    // 상품 옵션 상세 조회
    @GetMapping("/head/products/{productId}/options/{optionId}")
    public HeadProductOptionResponseDTO getProductOptionDetail(
            @PathVariable Integer productId,
            @PathVariable Integer optionId) {

        return headProductOptionService.getProductOptionDetail(productId, optionId);
    }

    // 상품 옵션 수정
    @PutMapping("/head/products/{productId}/options/{optionId}")
    public String updateProductOption(
            @PathVariable Integer productId,
            @PathVariable Integer optionId,
            @RequestBody HeadProductOptionUpdateRequestDTO requestDTO) {

        return headProductOptionService.updateProductOption(productId, optionId, requestDTO);
    }

    // 상품 옵션 삭제
    @DeleteMapping("/head/products/{productId}/options/{optionId}")
    public String deleteProductOption(
            @PathVariable Integer productId,
            @PathVariable Integer optionId) {

        return headProductOptionService.deleteProductOption(productId, optionId);
    }
}