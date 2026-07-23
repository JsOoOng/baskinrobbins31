package com.kiosk.customer.category.service;

import java.util.List;

import com.kiosk.customer.category.dto.CategoryResponse;

/**
 * [코드 흐름 안내] CategoryService
 *
 * <p>역할: 고객 키오스크의 상품 카테고리 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface CategoryService {

	List<CategoryResponse> getAllCategories();
}
