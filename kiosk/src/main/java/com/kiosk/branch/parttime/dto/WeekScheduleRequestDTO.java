package com.kiosk.branch.parttime.dto;

import java.time.LocalTime;

import com.kiosk.entity.enums.WorkDay;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeekScheduleRequestDTO {

    private WorkDay dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean isHoliday;

}