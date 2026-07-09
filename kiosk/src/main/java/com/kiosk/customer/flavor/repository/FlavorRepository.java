package com.kiosk.customer.flavor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlavorRepository extends JpaRepository<IcecreamFlavor, Integer> {
    
    // 특정 지점(storeId)에서 품절되지 않고 판매 중인 맛 목록 조회 (STORE_FLAVORS와 JOIN)
    @Query("SELECT f FROM IcecreamFlavor f JOIN StoreFlavor sf ON f.flavorId = sf.flavorId " +
           "WHERE sf.storeId = :storeId AND sf.isSoldOut = false AND f.isActive = true")
    List<IcecreamFlavor> findAvailableFlavorsByStoreId(@Param("storeId") Long storeId);
}