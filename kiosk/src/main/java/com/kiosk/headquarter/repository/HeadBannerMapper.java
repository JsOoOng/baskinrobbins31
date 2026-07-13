package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Banner;

@Repository
public interface HeadBannerMapper
        extends JpaRepository<Banner, Integer> {

    /*
     * 최근 등록한 배너부터 조회
     */
    List<Banner> findAllByOrderByIdDesc();
}