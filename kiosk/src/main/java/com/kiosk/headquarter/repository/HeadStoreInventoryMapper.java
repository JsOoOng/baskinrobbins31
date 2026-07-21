package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreInventory;
import com.kiosk.entity.enums.AutoRestockMode;

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