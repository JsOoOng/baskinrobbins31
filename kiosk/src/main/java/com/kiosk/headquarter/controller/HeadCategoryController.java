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

@RestController
@RequiredArgsConstructor
public class HeadCategoryController {

    private final HeadCategoryService headCategoryService;

    // 카테고리 등록
    @PostMapping("/head/categories")
    public HeadCategoryResponseDTO createCategory(
            @RequestBody HeadCategoryCreateRequestDTO requestDTO) {

        return headCategoryService.createCategory(requestDTO);
    }

    // 카테고리 목록 조회
    @GetMapping("/head/categories")
    public List<HeadCategoryResponseDTO> getCategoryList() {

        return headCategoryService.getCategoryList();
    }

    // 카테고리 상세 조회
    @GetMapping("/head/categories/{categoryId}")
    public HeadCategoryResponseDTO getCategoryDetail(
            @PathVariable Integer categoryId) {

        return headCategoryService.getCategoryDetail(categoryId);
    }

    // 카테고리 수정
    @PutMapping("/head/categories/{categoryId}")
    public HeadCategoryResponseDTO updateCategory(
            @PathVariable Integer categoryId,
            @RequestBody HeadCategoryUpdateRequestDTO requestDTO) {

        return headCategoryService.updateCategory(categoryId, requestDTO);
    }

    // 카테고리 삭제
    @DeleteMapping("/head/categories/{categoryId}")
    public String deleteCategory(
            @PathVariable Integer categoryId) {

        return headCategoryService.deleteCategory(categoryId);
    }
}