package com.kiosk.branch.statistics.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class StatisticsPeriod {

    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

}