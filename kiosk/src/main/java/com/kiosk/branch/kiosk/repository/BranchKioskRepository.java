package com.kiosk.branch.kiosk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Kiosk;


public interface BranchKioskRepository 
        extends JpaRepository<Kiosk, Integer> {


    // 지점별 키오스크 조회
    List<Kiosk> findByStore_Id(Integer storeId);


    // 같은 시리얼 번호 존재 여부
    boolean existsByDeviceSerial(String deviceSerial);


    // 같은 지점 키오스크 번호 중복 확인
    boolean existsByStore_IdAndKioskNumber(
            Integer storeId,
            Integer kioskNumber
    );

}