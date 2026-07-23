package com.kiosk.branch.parttime.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Staff;
import com.kiosk.entity.WeekSchedule;
import com.kiosk.entity.enums.StaffStatus;
import com.kiosk.entity.enums.WorkDay;


/**
 * [코드 흐름 안내] WeekScheduleRepository
 *
 * <p>역할: 지점 운영의 근무 일정 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(WEEK_SCHEDULES) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface WeekScheduleRepository 
        extends JpaRepository<WeekSchedule, Integer> {


    List<WeekSchedule> findByStaffOrderByDayOfWeek(
            Staff staff
    );


    Optional<WeekSchedule> findByStaffAndDayOfWeek(
            Staff staff,
            WorkDay dayOfWeek
    );


    List<WeekSchedule> findByStaff_Store_IdAndStaff_StatusNot(
            Integer storeId,
            StaffStatus status
    );


}