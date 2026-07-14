package com.kiosk.headquarter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreInventory;

@Repository
public interface HeadStoreInventoryMapper
        extends JpaRepository<StoreInventory, Integer> {

	boolean existsByStoreAndItem(Store store, InventoryItem item);

}