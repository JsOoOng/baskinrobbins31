package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Category;

/**
 * [코드 흐름 안내] HeadCategoryMapper
 *
 * <p>역할: 본사 관리의 상품 카테고리 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(CATEGORIES) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface HeadCategoryMapper extends JpaRepository<Category, Integer> {

    List<Category> findAllByOrderByDisplayOrderAscIdAsc();

    boolean existsByCategoryName(String categoryName);

    boolean existsByCategoryNameAndIdNot(String categoryName, Integer categoryId);
}
/*
findAllByOrderByDisplayOrderAscIdAsc()
→ displayOrder 오름차순, id 오름차순으로 카테고리 목록 조회

existsByCategoryName()
→ 등록 시 카테고리명 중복 확인

existsByCategoryNameAndIdNot()
→ 수정 시 자기 자신을 제외하고 카테고리명 중복 확인
*/