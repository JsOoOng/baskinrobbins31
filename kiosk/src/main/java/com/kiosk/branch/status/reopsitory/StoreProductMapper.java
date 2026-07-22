package com.kiosk.branch.status.reopsitory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreProduct;


public interface StoreProductMapper 
        extends JpaRepository<StoreProduct, Integer> {


    List<StoreProduct> findByStoreId(Integer storeId);
    
    Optional<StoreProduct> findByStoreIdAndProductId(
            Integer storeId,
            Integer productId
    );

}