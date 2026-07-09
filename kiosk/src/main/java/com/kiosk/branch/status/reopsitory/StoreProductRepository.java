package com.kiosk.branch.status.reopsitory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreProduct;


public interface StoreProductRepository 
        extends JpaRepository<StoreProduct, Integer> {


    List<StoreProduct> findByStoreStoreId(Integer storeId);

}