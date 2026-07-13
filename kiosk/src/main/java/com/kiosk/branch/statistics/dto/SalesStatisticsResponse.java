package com.kiosk.branch.statistics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalesStatisticsResponse {


    private String period;


    private Integer sales;


}