package com.kiosk.customer.category.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kiosk.customer.category.dto.CategoryResponse;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] CategoryServiceImpl
 *
 * <p>역할: 고객 키오스크의 상품 카테고리 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    // private final CategoryRepository categoryRepository; // 추후 주입 예정

    @Override
    /**
     * [메서드 흐름] getAllCategories
     * Controller 또는 상위 서비스에서 호출되어 입력값을 현재 클래스의 규칙에 따라 처리하고 호출한 곳으로 결과를 반환한다.
     */
    public List<CategoryResponse> getAllCategories() {
        // TODO: 레포지토리에서 CATEGORIES 테이블을 display_order 순으로 조회해와서 DTO로 변환 후 리턴
        return null;
    }
}