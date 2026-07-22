package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.HeadNotification;

@Repository
public interface HeadNotificationRepository
        extends JpaRepository<
                HeadNotification,
                Integer
        > {

    /*
     * 최신 알림 우선 조회
     */
    List<HeadNotification>
            findAllByOrderByCreatedAtDescIdDesc();
}