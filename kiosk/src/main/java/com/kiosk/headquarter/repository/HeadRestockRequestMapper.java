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
}