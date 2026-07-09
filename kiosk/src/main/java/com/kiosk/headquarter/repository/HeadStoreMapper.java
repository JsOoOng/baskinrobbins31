package com.kiosk.headquarter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Store;

@Repository
public interface HeadStoreMapper extends JpaRepository<Store, Integer> {
}