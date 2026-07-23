package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.RestockRequest;
import com.kiosk.entity.enums.RestockStatus;

@Repository
public interface HeadRestockRequestMapper extends JpaRepository<RestockRequest, Integer> {

    long countByStatus(RestockStatus status);

    List<RestockRequest> findAllByOrderByIdDesc();

    List<RestockRequest> findByStatusOrderByIdDesc(RestockStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT r FROM RestockRequest r LEFT JOIN r.storeInventory si LEFT JOIN r.storeFlavor sf WHERE si.store.id = :storeId OR sf.store.id = :storeId ORDER BY r.id DESC")
    List<RestockRequest> findByStoreIdOrderByIdDesc(@org.springframework.data.repository.query.Param("storeId") Integer storeId);
}