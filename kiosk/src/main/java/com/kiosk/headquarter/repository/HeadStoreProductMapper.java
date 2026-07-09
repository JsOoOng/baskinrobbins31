package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.StoreProduct;

@Repository
public interface HeadStoreProductMapper extends JpaRepository<StoreProduct, Integer> {

    // 특정 지점의 판매 상품 목록 조회
    List<StoreProduct> findByStore_IdOrderByIdDesc(Integer storeId);

    // 특정 지점의 특정 판매 상품 상세 조회
    Optional<StoreProduct> findByStore_IdAndId(Integer storeId, Integer storeProductId);

    // 특정 지점에 특정 본사 상품이 이미 등록되어 있는지 확인
    boolean existsByStore_IdAndProduct_Id(Integer storeId, Integer productId);
}