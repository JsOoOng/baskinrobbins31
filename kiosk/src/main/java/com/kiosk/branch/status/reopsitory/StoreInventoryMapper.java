package com.kiosk.branch.status.reopsitory;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreInventory;

public interface StoreInventoryMapper
        extends JpaRepository<StoreInventory, Integer> {


    Optional<StoreInventory> findByStoreIdAndItemProductId(
            Integer storeId,
            Integer productId
    );

}