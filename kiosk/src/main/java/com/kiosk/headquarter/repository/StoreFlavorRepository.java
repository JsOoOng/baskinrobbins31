package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreFlavor;
import com.kiosk.entity.enums.AutoRestockMode;

public interface StoreFlavorRepository
        extends JpaRepository<StoreFlavor, Integer> {


    /*
     * 전체 지점 맛 재고 조회
     */
    List<StoreFlavor> findAll();


    /*
     * 특정 지점 맛 재고 조회
     */
    List<StoreFlavor> findByStore_Id(
            Integer storeId
    );


    /*
     * 자동 보충 대상 조회용
     *
     * 추후 Scheduler에서 사용
     */
    List<StoreFlavor> findByAutoRestockEnabledTrue();
    
    List<StoreFlavor>
    findByAutoRestockEnabledTrueAndRestockModeIn(
            List<AutoRestockMode> modes
    );
}