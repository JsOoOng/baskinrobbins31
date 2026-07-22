package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreFlavor;

public interface StoreFlavorRestockRepository 
        extends JpaRepository<StoreFlavor, Integer> {


    List<StoreFlavor> findByAutoRestockEnabledTrue();

}