package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.store.HeadStoreProductAddRequestDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductDetailResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductListResponseDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductUpdateRequestDTO;
import com.kiosk.headquarter.service.HeadStoreProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HeadStoreProductController {

    private final HeadStoreProductService headStoreProductService;

    // 지점 판매 메뉴 등록
    @PostMapping("/head/stores/{storeId}/products")
    public String addStoreProduct(
            @PathVariable Integer storeId,
            @RequestBody HeadStoreProductAddRequestDTO requestDTO) {

        return headStoreProductService.addStoreProduct(storeId, requestDTO);
    }

    // 지점 판매 메뉴 목록 조회
    @GetMapping("/head/stores/{storeId}/products")
    public List<HeadStoreProductListResponseDTO> getStoreProductList(
            @PathVariable Integer storeId) {

        return headStoreProductService.getStoreProductList(storeId);
    }

    // 지점 판매 메뉴 상세 조회
    @GetMapping("/head/stores/{storeId}/products/{storeProductId}")
    public HeadStoreProductDetailResponseDTO getStoreProductDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer storeProductId) {

        return headStoreProductService.getStoreProductDetail(storeId, storeProductId);
    }

    // 지점 판매 메뉴 수정
    @PutMapping("/head/stores/{storeId}/products/{storeProductId}")
    public String updateStoreProduct(
            @PathVariable Integer storeId,
            @PathVariable Integer storeProductId,
            @RequestBody HeadStoreProductUpdateRequestDTO requestDTO) {

        return headStoreProductService.updateStoreProduct(storeId, storeProductId, requestDTO);
    }

    // 지점 판매 메뉴 삭제
    @DeleteMapping("/head/stores/{storeId}/products/{storeProductId}")
    public String deleteStoreProduct(
            @PathVariable Integer storeId,
            @PathVariable Integer storeProductId) {

        return headStoreProductService.deleteStoreProduct(storeId, storeProductId);
    }
}