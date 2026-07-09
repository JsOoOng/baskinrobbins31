package com.kiosk.customer.flavor.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Locale.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // 디비의 display_order 순으로 정렬하여 전체 카테고리 조회
    List<Category> findAllByOrderByDisplayOrderAsc();
}