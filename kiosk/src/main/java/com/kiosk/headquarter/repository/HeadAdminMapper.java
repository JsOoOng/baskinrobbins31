package com.kiosk.headquarter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

/**
 * [코드 흐름 안내] HeadAdminMapper
 *
 * <p>역할: 본사 관리의 본사 관리자 계정 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(headquarter_admins) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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