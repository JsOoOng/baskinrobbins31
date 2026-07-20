package com.kiosk.headquarter.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Employee;
import com.kiosk.entity.enums.Role;
import com.kiosk.headquarter.dto.employee.HeadEmployeeInsertDTO;

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