package com.kiosk.headquarter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.HeadquarterAdmin;
import com.kiosk.entity.enums.AdminRole;
import com.kiosk.entity.enums.AdminStatus;

@Repository
public interface HeadquarterAdminMapper extends JpaRepository<HeadquarterAdmin, Integer> {

    List<HeadquarterAdmin> findAllByOrderByIdDesc();

    Optional<HeadquarterAdmin> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    long countByRoleAndStatus(AdminRole role, AdminStatus status);
}