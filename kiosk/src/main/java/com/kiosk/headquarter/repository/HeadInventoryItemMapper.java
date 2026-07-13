package com.kiosk.headquarter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.InventoryItem;

@Repository
public interface HeadInventoryItemMapper extends JpaRepository<InventoryItem, Integer> {
}