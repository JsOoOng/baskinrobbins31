package com.kiosk.branch.statistics.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


/**
 * [코드 흐름 안내] DashboardStatisticsDto
 *
 * <p>역할: 통계 계층 사이에서 필요한 값만 전달하는 DTO다.</p>
 * <p>호출 흐름: Controller·Service·프론트 사이의 데이터 모양을 현재 필드 정의로 고정한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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