package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.StoreFlavor;

@Repository
public interface HeadStoreFlavorMapper extends JpaRepository<StoreFlavor, Integer> {

    List<StoreFlavor> findByStore_IdOrderByIdDesc(Integer storeId);

    Optional<StoreFlavor> findByStore_IdAndId(Integer storeId, Integer storeFlavorId);

    boolean existsByStore_IdAndFlavor_Id(Integer storeId, Integer flavorId);
}