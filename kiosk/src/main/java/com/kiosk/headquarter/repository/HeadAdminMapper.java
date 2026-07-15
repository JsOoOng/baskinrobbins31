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
     * 로그인 ID 중복 검사
     */
    boolean existsByLoginId(
            String loginId
    );

    /*
     * 관리자 목록 조회
     *
     * admin_id 내림차순
     * 최근 등록된 관리자부터 조회
     */
    List<HeadquarterAdmin>
            findAllByOrderByIdDesc();

    /*
     * 특정 역할과 상태를 가진 관리자 수 조회
     *
     * 마지막 활성 SUPER_ADMIN 보호에 사용
     */
    long countByRoleAndStatus(
            AdminRole role,
            AdminStatus status
    );
}