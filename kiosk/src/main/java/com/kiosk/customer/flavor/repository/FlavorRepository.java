package com.kiosk.customer.flavor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.IcecreamFlavor;

@Repository
public interface FlavorRepository extends JpaRepository<IcecreamFlavor, Integer> {

    // 특정 지점에서 판매 가능한 맛 조회
    @Query("""
        SELECT sf.flavor
        FROM StoreFlavor sf
        WHERE sf.store.id = :storeId
          AND sf.isSoldOut = false
          AND sf.flavor.isActive = true
    """)
    List<IcecreamFlavor> findAvailableFlavorsByStoreId(
            @Param("storeId") Long storeId
    );
}