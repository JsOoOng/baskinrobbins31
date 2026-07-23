package com.kiosk.headquarter.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.HeadquarterAdmin;

public interface HeadquarterAdminRepository 
        extends JpaRepository<HeadquarterAdmin, Integer> {


}