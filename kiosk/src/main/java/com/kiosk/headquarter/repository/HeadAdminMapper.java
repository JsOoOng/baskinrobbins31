package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

@Repository
public interface HeadAdminMapper
        extends JpaRepository<HeadquarterAdmin, Integer> {

    /*
     * 로그인 ID 중복 확인
     */
    boolean existsByLoginId(
            String loginId
    );

    /*
     * 최근 생성된 관리자부터 조회
     */
    List<HeadquarterAdmin>
            findAllByOrderByIdDesc();

    /*
     * 특정 역할·상태 관리자 수
     */
    long countByRoleAndStatus(
            AdminRole role,
            AdminStatus status
    );
}