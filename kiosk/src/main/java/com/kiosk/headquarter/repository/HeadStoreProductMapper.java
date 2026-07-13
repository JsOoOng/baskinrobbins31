package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.StoreProduct;

@Repository
public interface HeadStoreProductMapper
        extends JpaRepository<StoreProduct, Integer> {

    /*
     * 삭제되지 않은 동일 상품 존재 여부
     */
    boolean existsByStore_IdAndProduct_IdAndIsDeletedFalse(
            Integer storeId,
            Integer productId
    );

    /*
     * 지점 판매 상품 목록
     */
    List<StoreProduct>
            findByStore_IdAndIsDeletedFalseOrderByIdDesc(
                    Integer storeId
            );

    /*
     * 지점 판매 상품 상세 조회
     */
    Optional<StoreProduct>
            findByStore_IdAndIdAndIsDeletedFalse(
                    Integer storeId,
                    Integer storeProductId
            );
}