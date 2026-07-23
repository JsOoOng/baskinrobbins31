package com.kiosk.headquarter.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Employee;
import com.kiosk.entity.enums.Role;
import com.kiosk.headquarter.dto.employee.HeadEmployeeInsertDTO;

/**
 * [코드 흐름 안내] HeadEmployeeMapper
 *
 * <p>역할: 본사 관리의 직원 계정 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Mapper(EMPLOYEES) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface HeadEmployeeMapper
        extends JpaRepository<Employee, Integer> {

    /*
     * 로그인 ID로 직원 조회
     */
    Optional<Employee> findByLoginId(String loginId);

    /*
     * 로그인 ID 중복 여부 확인
     */
    boolean existsByLoginId(String loginId);

    /*
     * 특정 역할 중 하나를 가진 직원 조회
     *
     * 현재 사용하지 않는다면 삭제해도 됩니다.
     */
    Optional<Employee> findByLoginIdAndRoleIn(
            String loginId,
            Collection<Role> roles
    );
}