package com.kiosk.headquarter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.HeadquarterAdmin;

@Repository
public interface HeadquarterAdminMapper extends JpaRepository<HeadquarterAdmin, Integer> {
}