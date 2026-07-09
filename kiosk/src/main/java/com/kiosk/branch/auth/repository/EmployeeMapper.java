package com.kiosk.branch.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Employee;

public interface EmployeeMapper extends JpaRepository<Employee, Integer> {

    Optional<Employee> findByLoginId(String loginId);

}