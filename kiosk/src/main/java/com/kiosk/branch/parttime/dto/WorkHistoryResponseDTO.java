package com.kiosk.branch.parttime.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.kiosk.entity.enums.WorkDay;
import com.kiosk.entity.enums.WorkStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkHistoryResponseDTO {

    private Integer historyId;

    private LocalDate workDate;

    private WorkDay dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean isHoliday;

    private WorkStatus workStatus;

}