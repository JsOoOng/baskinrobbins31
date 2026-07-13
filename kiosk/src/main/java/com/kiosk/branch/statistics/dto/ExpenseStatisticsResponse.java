package com.kiosk.branch.statistics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpenseStatisticsResponse {


    private String category;


    private Integer amount;

}