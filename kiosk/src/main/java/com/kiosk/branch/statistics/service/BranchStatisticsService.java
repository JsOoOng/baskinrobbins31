package com.kiosk.branch.statistics.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kiosk.branch.statistics.dto.BranchStatisticsResponse;
import com.kiosk.branch.statistics.dto.CategorySalesDto;
import com.kiosk.branch.statistics.dto.DaySalesDto;
import com.kiosk.branch.statistics.dto.DashboardStatisticsDto;
import com.kiosk.branch.statistics.dto.ExpenseCategoryDto;
import com.kiosk.branch.statistics.dto.ExpenseDetailDto;
import com.kiosk.branch.statistics.dto.ExpensePaymentDto;
import com.kiosk.branch.statistics.dto.ExpensePeriodDto;
import com.kiosk.branch.statistics.dto.ProductRankDto;
import com.kiosk.branch.statistics.dto.ProductSalesDto;
import com.kiosk.branch.statistics.dto.SalesPeriodDto;
import com.kiosk.branch.statistics.dto.TimeOrderDto;
import com.kiosk.branch.statistics.dto.TimeSalesDto;
import com.kiosk.branch.statistics.repository.StatisticsExpenseRepository;
import com.kiosk.branch.statistics.repository.StatisticsOrderRepository;
import com.kiosk.branch.statistics.repository.StatisticsProductRepository;

import lombok.RequiredArgsConstructor;


/**
 * [코드 흐름 안내] BranchStatisticsService
 *
 * <p>역할: 지점 운영의 통계 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> StatisticsOrderRepository, StatisticsProductRepository, StatisticsExpenseRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
public class BranchStatisticsService {



private final StatisticsOrderRepository orderRepository;

private final StatisticsProductRepository productRepository;

private final StatisticsExpenseRepository expenseRepository;





public BranchStatisticsResponse getStatistics(

        Integer storeId,

        LocalDate startDate,

        LocalDate endDate,

        LocalTime startTime,

        LocalTime endTime

){


    LocalDateTime startDateTime =
            startDate.atTime(
                    startTime == null
                    ? LocalTime.MIN
                    : startTime
            );


    LocalDateTime endDateTime =
            endDate.atTime(
                    endTime == null
                    ? LocalTime.MAX
                    : endTime
            );





    /*
     * =========================
     * 기본 매출 / 주문
     * =========================
     */


    Integer sales =
            safeInteger(
                    orderRepository.findTotalSales(
                            storeId,
                            startDateTime,
                            endDateTime
                    )
            );



    Integer orderCount =
            safeInteger(
                    orderRepository.countCompletedOrders(
                            storeId,
                            startDateTime,
                            endDateTime
                    )
            );



    Integer totalOrderCount =
            safeInteger(
                    orderRepository.countAllOrders(
                            storeId,
                            startDateTime,
                            endDateTime
                    )
            );



    Integer canceledOrders =
            safeInteger(
                    orderRepository.countCanceledOrders(
                            storeId,
                            startDateTime,
                            endDateTime
                    )
            );





    Integer averageOrderPrice = 0;


    if(orderCount > 0){

        averageOrderPrice =
                sales / orderCount;

    }





    Double cancelRate = 0.0;


    if(totalOrderCount > 0){

        cancelRate =
                ((double)canceledOrders
                /
                totalOrderCount)
                * 100;

    }





    /*
     * =========================
     * 결제 / 할인
     * =========================
     */


    Integer totalPaymentAmount =
            safeInteger(
                    orderRepository.findTotalPaymentAmount(
                            storeId,
                            startDateTime,
                            endDateTime
                    )
            );



    Integer couponDiscountAmount =
            safeInteger(
                    orderRepository.findCouponDiscountAmount(
                            storeId,
                            startDateTime,
                            endDateTime
                    )
            );



    Integer pointAmount =
            safeInteger(
                    orderRepository.findPointAmount(
                            storeId,
                            startDateTime,
                            endDateTime
                    )
            );



    Integer finalPaymentAmount =
            safeInteger(
                    orderRepository.findFinalPaymentAmount(
                            storeId,
                            startDateTime,
                            endDateTime
                    )
            );





    /*
     * =========================
     * 매출 기간 통계
     * =========================
     */


    List<SalesPeriodDto> dailySales =
            orderRepository.findDailySales(
                    storeId,
                    startDateTime,
                    endDateTime
            )
            .stream()
            .map(row ->
                    new SalesPeriodDto(
                            String.valueOf(row[0]),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();





    List<SalesPeriodDto> monthlySales =
            orderRepository.findMonthlySales(
                    storeId,
                    startDateTime,
                    endDateTime
            )
            .stream()
            .map(row ->
                    new SalesPeriodDto(
                            row[0]+"-"+row[1],
                            ((Number)row[2]).intValue()
                    )
            )
            .toList();





    List<SalesPeriodDto> yearlySales =
            orderRepository.findYearlySales(
                    storeId,
                    startDateTime,
                    endDateTime
            )
            .stream()
            .map(row ->
                    new SalesPeriodDto(
                            String.valueOf(row[0]),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();
    
    /*
     * =========================
     * 시간 / 요일 통계
     * =========================
     */


    List<TimeSalesDto> hourlySales =
            orderRepository.findHourlySales(
                    storeId,
                    startDateTime,
                    endDateTime
            )
            .stream()
            .map(row ->
                    new TimeSalesDto(
                            ((Number)row[0]).intValue(),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();




    List<DaySalesDto> dayOfWeekSales =
            orderRepository.findDayOfWeekSales(
                    storeId,
                    startDateTime,
                    endDateTime
            )
            .stream()
            .map(row ->
                    new DaySalesDto(
                            ((Number)row[0]).intValue(),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();




    List<TimeOrderDto> hourlyOrderCount =
            orderRepository.findHourlyOrderCount(
                    storeId,
                    startDateTime,
                    endDateTime
            )
            .stream()
            .map(row ->
                    new TimeOrderDto(
                            ((Number)row[0]).intValue(),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();






    /*
     * =========================
     * 상품 통계
     * =========================
     */


    List<ProductRankDto> topProducts =
            productRepository.findTopSellingProducts(
                    storeId,
                    startDateTime,
                    endDateTime
            )
            .stream()
            .map(row ->
                    new ProductRankDto(
                            String.valueOf(row[0]),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();





    List<ProductSalesDto> productSales =
            productRepository.findProductSales(
                    storeId,
                    startDateTime,
                    endDateTime
            )
            .stream()
            .map(row ->
                    new ProductSalesDto(
                            String.valueOf(row[0]),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();





    List<CategorySalesDto> categorySales =
            productRepository.findCategorySales(
                    storeId,
                    startDateTime,
                    endDateTime
            )
            .stream()
            .map(row ->
                    new CategorySalesDto(
                            String.valueOf(row[0]),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();






    /*
     * =========================
     * 지출 통계
     *
     * StoreExpense는 현재
     * LocalDate 기준 유지
     * =========================
     */


    Integer expense =
            safeInteger(
                    expenseRepository.findTotalExpense(
                            storeId,
                            startDate,
                            endDate
                    )
            );




    List<ExpensePeriodDto> expensePeriod =
            expenseRepository.findDailyExpense(
                    storeId,
                    startDate,
                    endDate
            )
            .stream()
            .map(row ->
                    new ExpensePeriodDto(
                            String.valueOf(row[0]),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();





    List<ExpenseCategoryDto> expenseCategory =
            expenseRepository.findExpenseByCategory(
                    storeId,
                    startDate,
                    endDate
            )
            .stream()
            .map(row ->
                    new ExpenseCategoryDto(
                            String.valueOf(row[0]),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();





    List<ExpensePaymentDto> expensePayment =
            expenseRepository.findExpenseByPaymentMethod(
                    storeId,
                    startDate,
                    endDate
            )
            .stream()
            .map(row ->
                    new ExpensePaymentDto(
                            String.valueOf(row[0]),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();





    List<ExpenseDetailDto> expenseDetail =
            expenseRepository.findExpenseDetail(
                    storeId,
                    startDate,
                    endDate
            )
            .stream()
            .map(e ->
                    ExpenseDetailDto.builder()

                    .expenseId(e.getId())

                    .expenseDate(
                            e.getExpenseDate()
                    )

                    .description(
                            e.getDescription()
                    )

                    .amount(
                            e.getAmount()
                    )

                    .expenseCategory(
                            e.getExpenseCategory()
                    )

                    .paymentMethod(
                            e.getPaymentMethod()
                    )

                    .build()
            )
            .toList();







    /*
     * =========================
     * Response
     * =========================
     */


    return BranchStatisticsResponse.builder()


            .totalSales(sales)

            .orderCount(orderCount)

            .averageOrderPrice(
                    averageOrderPrice
            )


            .totalExpense(expense)


            .profit(
                    sales - expense
            )



            .totalPaymentAmount(
                    totalPaymentAmount
            )


            .couponDiscountAmount(
                    couponDiscountAmount
            )


            .pointAmount(
                    pointAmount
            )


            .finalPaymentAmount(
                    finalPaymentAmount
            )



            .dailySales(
                    dailySales
            )


            .monthlySales(
                    monthlySales
            )


            .yearlySales(
                    yearlySales
            )



            .hourlySales(
                    hourlySales
            )


            .dayOfWeekSales(
                    dayOfWeekSales
            )


            .hourlyOrderCount(
                    hourlyOrderCount
            )



            .topProducts(
                    topProducts
            )


            .productSales(
                    productSales
            )


            .categorySales(
                    categorySales
            )



            .canceledOrders(
                    canceledOrders
            )


            .cancelRate(
                    cancelRate
            )



            .expensePeriod(
                    expensePeriod
            )


            .expenseCategory(
                    expenseCategory
            )


            .expensePayment(
                    expensePayment
            )


            .expenseDetail(
                    expenseDetail
            )


            .build();

}





/*
 * null 방지
 */
private Integer safeInteger(Integer value){

    return value == null
            ? 0
            : value;

}





/*
 * =========================
 * 대시보드
 * 기존 유지
 * =========================
 */

public DashboardStatisticsDto getDashboard(
        Integer storeId
){


    Integer todaySales =
            safeInteger(
                    orderRepository.findTodaySales(
                            storeId
                    )
            );



    Integer todayOrders =
            safeInteger(
                    orderRepository.countTodayOrders(
                            storeId
                    )
            );



    List<TimeSalesDto> hourlySales =
            orderRepository.findTodayHourlySales(
                    storeId
            )
            .stream()
            .map(row ->
                    new TimeSalesDto(
                            ((Number)row[0]).intValue(),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();




    List<CategorySalesDto> categorySales =
            productRepository.findCategorySales(
                    storeId
            )
            .stream()
            .map(row ->
                    new CategorySalesDto(
                            String.valueOf(row[0]),
                            ((Number)row[1]).intValue()
                    )
            )
            .toList();




    return DashboardStatisticsDto.builder()

            .todaySales(
                    todaySales
            )

            .todayOrderCount(
                    todayOrders
            )

            .todayHourlySales(
                    hourlySales
            )

            .categorySales(
                    categorySales
            )

            .build();

}

}