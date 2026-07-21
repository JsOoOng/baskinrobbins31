package com.kiosk.branch.parttime.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Employee;

public interface EmployeeRepository 
        extends JpaRepository<Employee, Integer> {

}