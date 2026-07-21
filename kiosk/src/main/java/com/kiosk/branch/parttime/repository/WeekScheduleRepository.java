package com.kiosk.branch.parttime.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Staff;
import com.kiosk.entity.WeekSchedule;
import com.kiosk.entity.enums.StaffStatus;
import com.kiosk.entity.enums.WorkDay;


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