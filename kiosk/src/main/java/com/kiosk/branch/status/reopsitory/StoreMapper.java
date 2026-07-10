package com.kiosk.branch.status.reopsitory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Store;


public interface StoreMapper
        extends JpaRepository<Store, Integer> {


}