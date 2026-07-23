package com.kiosk.customer.product.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Product;

/**
 * [코드 흐름 안내] ProductRepository
 *
 * <p>역할: 고객 키오스크의 상품·메뉴 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(PRODUCTS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // 특정 지점의 특정 카테고리에 속한 상품 리스트와 해당 지점의 품절 여부를 함께 조회
    // (이 결과를 가지고 앞서 만든 ProductListResponse DTO로 변환하게 됩니다.)
	@Query("""
		    SELECT sp.product, sp.isSoldOut, sp.manualSoldOut
		    FROM StoreProduct sp
		    WHERE sp.store.id = :storeId
		    AND sp.product.category.id = :categoryId
		    AND sp.product.isDisplay = true
		""")
		List<Object[]> findProductsWithSoldOutStatus(
		        @Param("storeId") Long storeId,
		        @Param("categoryId") Long categoryId
		);
	Optional<Product> findById(Long valueOf);
}