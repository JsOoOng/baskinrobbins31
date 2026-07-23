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

/**
 * [코드 흐름 안내] HeadStoreProductController
 *
 * <p>역할: 본사 관리의 지점 판매 상품 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> HeadStoreProductService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class HeadStoreProductController {

    private final HeadStoreProductService headStoreProductService;

    // 지점 판매 메뉴 등록
    /**
     * [요청 흐름] POST /head/stores/{storeId}/products
     * 프론트 요청을 받아 addStoreProduct() 메서드가 입력을 받고 HeadStoreProductService 호출 후 결과를 응답한다.
     */
    @PostMapping("/head/stores/{storeId}/products")
    public String addStoreProduct(
            @PathVariable Integer storeId,
            @RequestBody HeadStoreProductAddRequestDTO requestDTO) {

        return headStoreProductService.addStoreProduct(storeId, requestDTO);
    }

    // 지점 판매 메뉴 목록 조회
    /**
     * [요청 흐름] GET /head/stores/{storeId}/products
     * 프론트 요청을 받아 getStoreProductList() 메서드가 입력을 받고 HeadStoreProductService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/stores/{storeId}/products")
    public List<HeadStoreProductListResponseDTO> getStoreProductList(
            @PathVariable Integer storeId) {

        return headStoreProductService.getStoreProductList(storeId);
    }

    // 지점 판매 메뉴 상세 조회
    /**
     * [요청 흐름] GET /head/stores/{storeId}/products/{storeProductId}
     * 프론트 요청을 받아 getStoreProductDetail() 메서드가 입력을 받고 HeadStoreProductService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/stores/{storeId}/products/{storeProductId}")
    public HeadStoreProductDetailResponseDTO getStoreProductDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer storeProductId) {

        return headStoreProductService.getStoreProductDetail(storeId, storeProductId);
    }

    // 지점 판매 메뉴 수정
    /**
     * [요청 흐름] PUT /head/stores/{storeId}/products/{storeProductId}
     * 프론트 요청을 받아 updateStoreProduct() 메서드가 입력을 받고 HeadStoreProductService 호출 후 결과를 응답한다.
     */
    @PutMapping("/head/stores/{storeId}/products/{storeProductId}")
    public String updateStoreProduct(
            @PathVariable Integer storeId,
            @PathVariable Integer storeProductId,
            @RequestBody HeadStoreProductUpdateRequestDTO requestDTO) {

        return headStoreProductService.updateStoreProduct(storeId, storeProductId, requestDTO);
    }

    // 지점 판매 메뉴 삭제
    /**
     * [요청 흐름] DELETE /head/stores/{storeId}/products/{storeProductId}
     * 프론트 요청을 받아 deleteStoreProduct() 메서드가 입력을 받고 HeadStoreProductService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/head/stores/{storeId}/products/{storeProductId}")
    public String deleteStoreProduct(
            @PathVariable Integer storeId,
            @PathVariable Integer storeProductId) {

        return headStoreProductService.deleteStoreProduct(storeId, storeProductId);
    }
}