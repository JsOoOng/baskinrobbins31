package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Product;

@Repository
public interface HeadProductMapper extends JpaRepository<Product, Integer> {

    // 화면 표시 중인 상품 목록
    List<Product> findByIsDisplayTrueOrderByIdDesc();

    // 화면 표시 중인 상품 상세 조회
    Optional<Product> findByIdAndIsDisplayTrue(Integer productId);
    
    int countByCategory_Id(Integer categoryId);
}