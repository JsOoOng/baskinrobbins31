package com.kiosk.customer.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.IcecreamFlavor;

@Repository
public interface IcecreamFlavorRepository extends JpaRepository<IcecreamFlavor, Integer> {
    // 현재 판매 운영 중(isActive = true)인 맛 리스트만 조회
    List<IcecreamFlavor> findByIsActiveTrue();
}