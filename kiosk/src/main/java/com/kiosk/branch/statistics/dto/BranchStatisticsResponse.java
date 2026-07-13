package com.kiosk.branch.statistics.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;



@Getter
@Builder
@AllArgsConstructor
public class BranchStatisticsResponse {



    /*
     * ==========================
     * 기본 매출 통계
     * ==========================
     */


    // 총 매출
    private Integer totalSales;


    // 완료 주문 건수
    private Integer orderCount;


    // 객단가
    private Integer averageOrderPrice;


    // 총 지출
    private Integer totalExpense;


    // 순이익
    private Integer profit;




    /*
     * ==========================
     * 기간별 매출
     * ==========================
     */


    // 일별 매출
    private List<SalesPeriodDto> dailySales;


    // 월별 매출
    private List<SalesPeriodDto> monthlySales;


    // 연별 매출
    private List<SalesPeriodDto> yearlySales;




    /*
     * ==========================
     * 시간 / 요일 분석
     * ==========================
     */


    // 시간대별 매출
    private List<TimeSalesDto> hourlySales;


    // 요일별 매출
    private List<DaySalesDto> dayOfWeekSales;



    // 시간대별 주문량
    private List<TimeOrderDto> hourlyOrderCount;




    /*
     * ==========================
     * 상품 통계
     * ==========================
     */


    // 베스트 상품 TOP N
    private List<ProductRankDto> topProducts;


    // 상품별 매출
    private List<ProductSalesDto> productSales;


    // 카테고리별 매출
    private List<CategorySalesDto> categorySales;




    /*
     * ==========================
     * 취소 분석
     * ==========================
     */


    // 취소 주문 수
    private Integer canceledOrders;


    // 취소율
    private Double cancelRate;




    /*
     * ==========================
     * 지출 분석
     * ==========================
     */


    // 기간별 지출
    private List<ExpensePeriodDto> expensePeriod;


    // 지출 카테고리
    private List<ExpenseCategoryDto> expenseCategory;


    // 지출 결제 방식
    private List<ExpensePaymentDto> expensePayment;

}