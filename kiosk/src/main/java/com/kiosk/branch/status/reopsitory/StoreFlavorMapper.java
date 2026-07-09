package com.kiosk.branch.status.reopsitory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreFlavor;


public interface StoreFlavorMapper 
        extends JpaRepository<StoreFlavor, Integer> {


    List<StoreFlavor> findByStoreId(Integer storeId);
    

}