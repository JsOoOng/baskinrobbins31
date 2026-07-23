package com.kiosk.headquarter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Product;

/**
 * [코드 흐름 안내] HeadInventoryItemMapper
 *
 * <p>역할: 본사 관리의 재고 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(INVENTORY_ITEMS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface HeadInventoryItemMapper
        extends JpaRepository<InventoryItem, Integer> {

    Optional<InventoryItem> findByProduct(Product product);

}