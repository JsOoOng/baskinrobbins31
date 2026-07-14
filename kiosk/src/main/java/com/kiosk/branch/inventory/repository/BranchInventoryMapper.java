package com.kiosk.branch.inventory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreInventory;

public interface BranchInventoryMapper 
        extends JpaRepository<StoreInventory, Integer> {


    Optional<StoreInventory> 
    findByStore_IdAndItem_Id(
            Integer storeId,
            Integer itemId
    );

}