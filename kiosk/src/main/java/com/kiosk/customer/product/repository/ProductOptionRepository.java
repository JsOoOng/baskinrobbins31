package com.kiosk.customer.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.ProductOption;

/**
 * [코드 흐름 안내] ProductOptionRepository
 *
 * <p>역할: 고객 키오스크의 상품 옵션 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(PRODUCT_OPTIONS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Integer> {
    
    // 특정 상품에 걸려있는 옵션 리스트 조회
    List<ProductOption> findByProductId(Integer productId);

    // 🚀 [하드코딩 제로 핵심 쿼리] 상품 ID와 옵션 타입을 조건으로 maxFlavorCount만 쏙 뽑아옵니다.
    @Query("SELECT po.maxFlavorCount FROM ProductOption po WHERE po.product.Id = :productId AND po.optionType = 'SIZE'")
    Integer findMaxFlavorCountByProductId(@Param("productId") Integer productId);
}