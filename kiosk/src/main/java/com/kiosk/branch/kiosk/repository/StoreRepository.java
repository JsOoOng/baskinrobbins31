package com.kiosk.branch.kiosk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {

}