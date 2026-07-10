package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Category;

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