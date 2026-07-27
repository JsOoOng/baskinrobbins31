package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Product;

/**
 * [코드 흐름 안내] HeadProductMapper
 *
 * <p>역할: 본사 관리의 상품·메뉴 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(PRODUCTS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface HeadProductMapper extends JpaRepository<Product, Integer> {

    // 화면 표시 중인 상품 목록
    List<Product> findByIsDisplayTrueOrderByIdDesc();
    List<Product> findAllByOrderByIdDesc();

    // 화면 표시 중인 상품 상세 조회
    Optional<Product> findByIdAndIsDisplayTrue(Integer productId);
    
    long countByCategory_Id(Integer categoryId);

    long countByDiscountRateGreaterThan(Integer discountRate);
}
