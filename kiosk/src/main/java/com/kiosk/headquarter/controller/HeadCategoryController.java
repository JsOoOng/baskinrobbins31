package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.category.HeadCategoryCreateRequestDTO;
import com.kiosk.headquarter.dto.category.HeadCategoryResponseDTO;
import com.kiosk.headquarter.dto.category.HeadCategoryUpdateRequestDTO;
import com.kiosk.headquarter.service.HeadCategoryService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadCategoryController
 *
 * <p>역할: 본사 관리의 상품 카테고리 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> HeadCategoryService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class HeadCategoryController {

    private final HeadCategoryService headCategoryService;

    // 카테고리 등록
    /**
     * [요청 흐름] POST /head/categories
     * 프론트 요청을 받아 createCategory() 메서드가 입력을 받고 HeadCategoryService 호출 후 결과를 응답한다.
     */
    @PostMapping("/head/categories")
    public HeadCategoryResponseDTO createCategory(
            @RequestBody HeadCategoryCreateRequestDTO requestDTO) {

        return headCategoryService.createCategory(requestDTO);
    }

    // 카테고리 목록 조회
    /**
     * [요청 흐름] GET /head/categories
     * 프론트 요청을 받아 getCategoryList() 메서드가 입력을 받고 HeadCategoryService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/categories")
    public List<HeadCategoryResponseDTO> getCategoryList() {

        return headCategoryService.getCategoryList();
    }

    // 카테고리 상세 조회
    /**
     * [요청 흐름] GET /head/categories/{categoryId}
     * 프론트 요청을 받아 getCategoryDetail() 메서드가 입력을 받고 HeadCategoryService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/categories/{categoryId}")
    public HeadCategoryResponseDTO getCategoryDetail(
            @PathVariable Integer categoryId) {

        return headCategoryService.getCategoryDetail(categoryId);
    }

    // 카테고리 수정
    /**
     * [요청 흐름] PUT /head/categories/{categoryId}
     * 프론트 요청을 받아 updateCategory() 메서드가 입력을 받고 HeadCategoryService 호출 후 결과를 응답한다.
     */
    @PutMapping("/head/categories/{categoryId}")
    public HeadCategoryResponseDTO updateCategory(
            @PathVariable Integer categoryId,
            @RequestBody HeadCategoryUpdateRequestDTO requestDTO) {

        return headCategoryService.updateCategory(categoryId, requestDTO);
    }

    // 카테고리 삭제
    /**
     * [요청 흐름] DELETE /head/categories/{categoryId}
     * 프론트 요청을 받아 deleteCategory() 메서드가 입력을 받고 HeadCategoryService 호출 후 결과를 응답한다.
     */
    @DeleteMapping("/head/categories/{categoryId}")
    public String deleteCategory(
            @PathVariable Integer categoryId) {

        return headCategoryService.deleteCategory(categoryId);
    }
}