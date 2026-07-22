package com.kiosk.branch.parttime.dto;

import java.time.LocalTime;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkHistoryUpdateRequestDTO {

    private LocalTime startTime;

    private LocalTime endTime;

    private Boolean isHoliday;

}