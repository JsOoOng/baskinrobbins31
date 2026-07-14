package com.kiosk.headquarter.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.InventoryItem;
import com.kiosk.entity.Product;

@Repository
public interface HeadInventoryItemMapper
        extends JpaRepository<InventoryItem, Integer> {

    Optional<InventoryItem> findByProduct(Product product);

}