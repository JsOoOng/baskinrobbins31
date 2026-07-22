package com.kiosk.headquarter.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.entity.Category;
import com.kiosk.headquarter.dto.category.HeadCategoryCreateRequestDTO;
import com.kiosk.headquarter.dto.category.HeadCategoryResponseDTO;
import com.kiosk.headquarter.dto.category.HeadCategoryUpdateRequestDTO;
import com.kiosk.headquarter.repository.HeadCategoryMapper;
import com.kiosk.headquarter.repository.HeadProductMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeadCategoryService {

    private final HeadCategoryMapper headCategoryMapper;
    private final HeadProductMapper headProductMapper;
    private final AdminLogService adminLogService;

    // 카테고리 등록
    @Transactional
    public HeadCategoryResponseDTO createCategory(HeadCategoryCreateRequestDTO requestDTO) {

        if (requestDTO.getCategoryName() == null || requestDTO.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("카테고리명을 입력해주세요.");
        }

        boolean alreadyExists = headCategoryMapper.existsByCategoryName(requestDTO.getCategoryName());

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다.");
        }

        Category category = Category.builder()
                .categoryName(requestDTO.getCategoryName())
                .displayOrder(
                        requestDTO.getDisplayOrder() != null
                                ? requestDTO.getDisplayOrder()
                                : 0
                )
                .active(
                        requestDTO.getActive() != null
                                ? requestDTO.getActive()
                                : true
                )
                .build();

        Category savedCategory = headCategoryMapper.save(category);

        adminLogService.logAction("카테고리", savedCategory.getCategoryName() + " 카테고리 신규 등록");

        return toResponseDTO(savedCategory);
    }

    // 카테고리 목록 조회
    public List<HeadCategoryResponseDTO> getCategoryList() {

        return headCategoryMapper.findAllByOrderByDisplayOrderAscIdAsc()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // 카테고리 상세 조회
    public HeadCategoryResponseDTO getCategoryDetail(Integer categoryId) {

        Category category = headCategoryMapper.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        return toResponseDTO(category);
    }

    // 카테고리 수정
    @Transactional
    public HeadCategoryResponseDTO updateCategory(
            Integer categoryId,
            HeadCategoryUpdateRequestDTO requestDTO) {

        Category category = headCategoryMapper.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        if (requestDTO.getCategoryName() == null || requestDTO.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("카테고리명을 입력해주세요.");
        }

        boolean alreadyExists = headCategoryMapper.existsByCategoryNameAndIdNot(
                requestDTO.getCategoryName(),
                categoryId
        );

        if (alreadyExists) {
            throw new IllegalArgumentException("이미 존재하는 카테고리명입니다.");
        }

        category.updateCategory(
                requestDTO.getCategoryName(),

                requestDTO.getDisplayOrder() != null
                        ? requestDTO.getDisplayOrder()
                        : 0,

                requestDTO.getActive()
        );

        adminLogService.logAction("카테고리", category.getCategoryName() + " 카테고리 정보 수정");

        return toResponseDTO(category);
    }

    // 카테고리 삭제
    @Transactional
    public String deleteCategory(Integer categoryId) {

        Category category = headCategoryMapper.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        long productCount = headProductMapper.countByCategory_Id(categoryId);

        if (productCount > 0) {
            throw new IllegalArgumentException("해당 카테고리를 사용하는 상품이 있어 삭제할 수 없습니다.");
        }

        headCategoryMapper.delete(category);

        adminLogService.logAction("카테고리", category.getCategoryName() + " 카테고리 삭제");

        return "카테고리 삭제 성공";
    }

    private HeadCategoryResponseDTO toResponseDTO(
            Category category
    ) {
        return HeadCategoryResponseDTO.builder()
                .categoryId(category.getId())
                .categoryName(category.getCategoryName())
                .displayOrder(category.getDisplayOrder())
                .active(category.getActive())
                .build();
    }
}