package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import com.kiosk.headquarter.dto.store.HeadStoreProductAddRequestDTO;
import com.kiosk.headquarter.dto.store.HeadStoreProductListResponseDTO;
import com.kiosk.headquarter.service.HeadStoreProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import com.kiosk.headquarter.dto.store.HeadStoreProductUpdateRequestDTO;

@RestController
@RequiredArgsConstructor
public class HeadStoreProductController {

    private final HeadStoreProductService headStoreProductService;

    @PostMapping("/head/stores/{storeId}/products")
    public String addStoreProduct(
            @PathVariable Integer storeId,
            @RequestBody HeadStoreProductAddRequestDTO requestDTO) {

        boolean result = headStoreProductService.addStoreProduct(storeId, requestDTO);

        if (result) {
            return "지점 판매 메뉴 등록 성공";
        }

        return "지점 판매 메뉴 등록 실패";
    }
    
    @PutMapping("/head/stores/{storeId}/products/{storeProductId}")
    public String updateStoreProduct(
            @PathVariable Integer storeId,
            @PathVariable Integer storeProductId,
            @RequestBody HeadStoreProductUpdateRequestDTO requestDTO) {

        boolean result = headStoreProductService.updateStoreProduct(storeId, storeProductId, requestDTO);

        if (result) {
            return "지점 판매 메뉴 수정 성공";
        }

        return "지점 판매 메뉴 수정 실패";
    }
    
    @DeleteMapping("/head/stores/{storeId}/products/{storeProductId}")
    public String deleteStoreProduct(
            @PathVariable Integer storeId,
            @PathVariable Integer storeProductId) {

        boolean result = headStoreProductService.deleteStoreProduct(storeId, storeProductId);

        if (result) {
            return "지점 판매 메뉴 삭제 성공";
        }

        return "지점 판매 메뉴 삭제 실패";
    }

    @GetMapping("/head/stores/{storeId}/products")
    public List<HeadStoreProductListResponseDTO> getStoreProductList(@PathVariable Integer storeId) {
        return headStoreProductService.getStoreProductList(storeId);
    }
}