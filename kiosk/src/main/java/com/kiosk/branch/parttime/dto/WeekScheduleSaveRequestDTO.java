package com.kiosk.branch.parttime.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WeekScheduleSaveRequestDTO {

    private List<WeekScheduleRequestDTO> schedules;

}