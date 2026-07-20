package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Store;

@Repository
public interface HeadStoreMapper
        extends JpaRepository<Store, Integer> {

    /*
     * 최신 등록 지점부터 조회
     */
    List<Store> findAllByOrderByIdDesc();

    /*
     * 사업자 번호 중복 확인
     */
    boolean existsByBusinessNumber(
            String businessNumber
    );
}