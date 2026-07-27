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

/**
 * [코드 흐름 안내] HeadProductOptionController
 *
 * <p>역할: 본사 관리의 상품 옵션 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> HeadProductOptionService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class HeadProductOptionController {

    private final HeadProductOptionService headProductOptionService;

    // 상품 옵션 등록
    /**
     * [요청 흐름] POST /head/products/{productId}/options
     * 프론트 요청을 받아 createProductOption() 메서드가 입력을 받고 HeadProductOptionService 호출 후 결과를 응답한다.
     */
    @PostMapping("/head/products/{productId}/options")
    public String createProductOption(
            @PathVariable Integer productId,
            @RequestBody HeadProductOptionCreateRequestDTO requestDTO) {

        return headProductOptionService.createProductOption(productId, requestDTO);
    }

    // 상품 옵션 목록 조회
    /**
     * [요청 흐름] GET /head/products/{productId}/options
     * 프론트 요청을 받아 getProductOptionList() 메서드가 입력을 받고 HeadProductOptionService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/products/{productId}/options")
    public List<HeadProductOptionResponseDTO> getProductOptionList(
            @PathVariable Integer productId) {

        return headProductOptionService.getProductOptionList(productId);
    }

    // 상품 옵션 상세 조회
    /**
     * [요청 흐름] GET /head/products/{productId}/options/{optionId}
     * 프론트 요청을 받아 getProductOptionDetail() 메서드가 입력을 받고 HeadProductOptionService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/products/{productId}/options/{optionId}")
    public HeadProductOptionResponseDTO getProductOptionDetail(
            @PathVariable Integer productId,
            @PathVariable Integer optionId) {

        return headProductOptionService.getProductOptionDetail(productId, optionId);
    }

    // 상품 옵션 수정
    /**
     * [요청 흐름] PUT /head/products/{productId}/options/{optionId}
     * 프론트 요청을 받아 updateProductOption() 메서드가 입력을 받고 HeadProductOptionService 호출 후 결과를 응답한다.
     */
    @PutMapping("/head/products/{productId}/options/{optionId}")
    public String updateProductOption(
            @PathVariable Integer productId,
            @PathVariable Integer optionId,
            @RequestBody HeadProductOptionUpdateRequestDTO requestDTO) {

        return headProductOptionService.updateProductOption(productId, optionId, requestDTO);
    }

    // 상품 옵션 삭제
    /**
     * [요청 흐름] DELETE /head/products/{productId}/options/{optionId}
     * 프론트 요청을 받아 deleteProductOption() 메서드가 입력을 받고 HeadProductOptionService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/head/products/{productId}/options/{optionId}")
    public String deleteProductOption(
            @PathVariable Integer productId,
            @PathVariable Integer optionId) {

        return headProductOptionService.deleteProductOption(productId, optionId);
    }
}