package com.kiosk.branch.parttime.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Staff;
import com.kiosk.entity.WorkHistory;

/**
 * [코드 흐름 안내] WorkHistoryRepository
 *
 * <p>역할: 지점 운영의 근무 이력 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(WORK_HISTORY) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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