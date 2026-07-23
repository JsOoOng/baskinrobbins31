package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.StoreProduct;

/**
 * [코드 흐름 안내] HeadStoreProductMapper
 *
 * <p>역할: 본사 관리의 지점 판매 상품 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(STORE_PRODUCTS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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