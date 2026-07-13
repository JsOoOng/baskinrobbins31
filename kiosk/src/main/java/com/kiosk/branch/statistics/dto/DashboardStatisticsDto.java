package com.kiosk.branch.statistics.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor
public class DashboardStatisticsDto {


    // 오늘 매출
    private Integer todaySales;


    // 오늘 주문 건수
    private Integer todayOrderCount;


    // 오늘 시간대별 매출
    private List<TimeSalesDto> todayHourlySales;


    // 카테고리별 판매 비중
    private List<CategorySalesDto> categorySales;


}