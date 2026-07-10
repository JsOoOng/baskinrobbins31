package com.kiosk.customer.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.ProductOption;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Integer> {
    
    // 특정 상품에 걸려있는 옵션 리스트 조회
    List<ProductOption> findByProductId(Integer productId);

    // 🚀 [하드코딩 제로 핵심 쿼리] 상품 ID와 옵션 타입을 조건으로 maxFlavorCount만 쏙 뽑아옵니다.
    @Query("SELECT po.maxFlavorCount FROM ProductOption po WHERE po.product.Id = :productId AND po.optionType = 'SIZE'")
    Integer findMaxFlavorCountByProductId(@Param("productId") Integer productId);
}