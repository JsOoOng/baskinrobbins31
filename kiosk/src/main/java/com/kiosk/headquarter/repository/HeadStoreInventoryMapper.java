package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.AutoRestockMode;

/**
 * [코드 흐름 안내] HeadStoreInventoryMapper
 *
 * <p>역할: 본사 관리의 지점 재고 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(STORE_INVENTORY) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface HeadStoreInventoryMapper
        extends JpaRepository<
                StoreInventory,
                Integer
        > {

    boolean existsByStoreAndItem(
            Store store,
            InventoryItem item
    );

    Optional<StoreInventory>
            findByStoreAndItem(
                    Store store,
                    InventoryItem item
            );

    /*
     * 자동 보충이 활성화되어 있고,
     * 재고 보충 방식이 전달한 목록에 포함된 재고 조회
     *
     * DAILY 또는 BOTH 조회에 사용
     */
    List<StoreInventory>
            findByAutoRestockEnabledTrueAndRestockModeIn(
                    List<AutoRestockMode> modes
            );
}