package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.StoreProduct;

@Repository
public interface HeadStoreProductMapper
        extends JpaRepository<
                StoreProduct,
                Integer
        > {

    /*
     * 해당 지점에 활성 상태로 연결된
     * 동일 상품이 존재하는지 확인
     */
    boolean
            existsByStore_IdAndProduct_IdAndIsDeletedFalse(
                    Integer storeId,
                    Integer productId
            );

    /*
     * 특정 지점의 삭제되지 않은 판매 메뉴 목록
     */
    List<StoreProduct>
            findByStore_IdAndIsDeletedFalseOrderByIdDesc(
                    Integer storeId
            );

    /*
     * 특정 지점의 판매 메뉴 상세 조회
     */
    Optional<StoreProduct>
            findByStore_IdAndIdAndIsDeletedFalse(
                    Integer storeId,
                    Integer storeProductId
            );
}