package com.kiosk.branch.parttime.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import com.kiosk.entity.enums.WorkDay;


@Getter
@Builder
@AllArgsConstructor
public class StoreWeekScheduleResponseDTO {


    private WorkDay dayOfWeek;


    private Integer staffId;


    private String staffName;


    private String startTime;


    private String endTime;


}