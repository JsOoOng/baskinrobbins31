package com.kiosk.customer.category.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Category;

/**
 * [코드 흐름 안내] CategoryRepository
 *
 * <p>역할: 고객 키오스크의 상품 카테고리 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(CATEGORIES) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // 디비의 display_order 순으로 정렬하여 전체 카테고리 조회
    List<Category> findAllByOrderByDisplayOrderAsc();
}