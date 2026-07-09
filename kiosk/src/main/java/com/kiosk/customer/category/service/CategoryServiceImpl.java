package com.kiosk.customer.category.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kiosk.customer.category.dto.CategoryResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    // private final CategoryRepository categoryRepository; // 추후 주입 예정

    @Override
    public List<CategoryResponse> getAllCategories() {
        // TODO: 레포지토리에서 CATEGORIES 테이블을 display_order 순으로 조회해와서 DTO로 변환 후 리턴
        return null;
    }
}