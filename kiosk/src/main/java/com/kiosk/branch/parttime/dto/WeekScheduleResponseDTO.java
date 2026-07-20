package com.kiosk.branch.parttime.dto;

import java.time.LocalTime;

import com.kiosk.entity.enums.WorkDay;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeekScheduleResponseDTO {

    private Integer scheduleId;

    private WorkDay dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean isHoliday;

}