package com.kiosk.branch.parttime.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Staff;
import com.kiosk.entity.WorkHistory;

public interface WorkHistoryRepository extends JpaRepository<WorkHistory, Integer> {

    List<WorkHistory> findByStaffAndWorkDateBetweenOrderByWorkDateAsc(
            Staff staff,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<WorkHistory> findByStaffAndWorkDate(
            Staff staff,
            LocalDate workDate
    );

    boolean existsByStaffAndWorkDate(
            Staff staff,
            LocalDate workDate
    );

}