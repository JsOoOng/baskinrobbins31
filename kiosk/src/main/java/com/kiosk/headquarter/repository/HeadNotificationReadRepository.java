package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.HeadNotificationRead;

@Repository
public interface HeadNotificationReadRepository
        extends JpaRepository<
                HeadNotificationRead,
                Integer
        > {

    /*
     * 특정 관리자가 특정 알림을
     * 이미 읽었는지 확인
     */
    boolean existsByNotification_IdAndAdminId(
            Integer notificationId,
            Integer adminId
    );

    /*
     * 특정 알림 읽음 기록 조회
     */
    Optional<HeadNotificationRead>
            findByNotification_IdAndAdminId(
                    Integer notificationId,
                    Integer adminId
            );

    /*
     * 특정 관리자의 읽음 기록 개수
     */
    long countByAdminId(
            Integer adminId
    );

    /*
     * 특정 관리자가 읽은 알림 번호만 조회
     */
    @Query("""
            SELECT notificationRead.notification.id
            FROM HeadNotificationRead notificationRead
            WHERE notificationRead.adminId = :adminId
            """)
    List<Integer> findReadNotificationIds(
            @Param("adminId")
            Integer adminId
    );
}