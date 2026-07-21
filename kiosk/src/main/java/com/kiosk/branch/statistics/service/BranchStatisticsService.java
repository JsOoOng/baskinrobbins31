package com.kiosk.branch.statistics.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kiosk.branch.statistics.dto.BranchStatisticsResponse;
import com.kiosk.branch.statistics.dto.CategorySalesDto;
import com.kiosk.branch.statistics.dto.DashboardStatisticsDto;
import com.kiosk.branch.statistics.dto.DaySalesDto;
import com.kiosk.branch.statistics.dto.ExpenseCategoryDto;
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


@Service
@RequiredArgsConstructor
public class BranchStatisticsService {


    private final StatisticsOrderRepository orderRepository;

    private final StatisticsProductRepository productRepository;

    private final StatisticsExpenseRepository expenseRepository;




    public BranchStatisticsResponse getStatistics(
    	    Integer storeId,
    	    LocalDate startDate,
    	    LocalDate endDate
    	){


        /*
         * =========================
         * 기본 매출 / 지출
         * =========================
         */


        Integer sales =
                safeInteger(
                    orderRepository.findTotalSales(storeId, startDate, endDate)
                );


        Integer orderCount =
                safeInteger(
                    orderRepository.countCompletedOrders(storeId, startDate, endDate)
                );


        Integer totalOrderCount =
                safeInteger(
                    orderRepository.countAllOrders(storeId, startDate, endDate)
                );



        Integer expense =
                safeInteger(
                    expenseRepository.findTotalExpense(storeId, startDate, endDate)
                );



        Integer averageOrderPrice = 0;


        if(orderCount > 0){

            averageOrderPrice =
                    sales / orderCount;

        }




        Integer canceledOrders =
                safeInteger(
                    orderRepository.countCanceledOrders(storeId, startDate, endDate)
                );




        Double cancelRate = 0.0;


        if(totalOrderCount > 0){

            cancelRate =
                    ((double)canceledOrders 
                    / totalOrderCount)
                    * 100;

        }


        /*
         * =========================
         * 결제 / 할인 / 마일리지
         * =========================
         */

        Integer totalPaymentAmount =
                safeInteger(
                        orderRepository.findTotalPaymentAmount(
                                storeId,
                                startDate,
                                endDate
                        )
                );


        Integer couponDiscountAmount =
                safeInteger(
                        orderRepository.findCouponDiscountAmount(
                                storeId,
                                startDate,
                                endDate
                        )
                );


        Integer pointAmount =
                safeInteger(
                        orderRepository.findPointAmount(
                                storeId,
                                startDate,
                                endDate
                        )
                );


        Integer finalPaymentAmount =
                safeInteger(
                        orderRepository.findFinalPaymentAmount(
                                storeId,
                                startDate,
                                endDate
                        )
                );
        
        




        /*
         * =========================
         * 매출 기간 통계
         * =========================
         */



        List<SalesPeriodDto> dailySales =
                orderRepository.findDailySales(storeId, startDate, endDate)
                .stream()
                .map(row ->
                    new SalesPeriodDto(
                        String.valueOf(row[0]),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();





        List<SalesPeriodDto> monthlySales =
                orderRepository.findMonthlySales(storeId, startDate, endDate)
                .stream()
                .map(row ->
                    new SalesPeriodDto(
                        row[0]+"-"+row[1],
                        ((Number)row[2]).intValue()
                    )
                )
                .toList();





        List<SalesPeriodDto> yearlySales =
                orderRepository.findYearlySales(storeId, startDate, endDate)
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
                orderRepository.findHourlySales(storeId, startDate, endDate)
                .stream()
                .map(row ->
                    new TimeSalesDto(
                        ((Number)row[0]).intValue(),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();





        List<DaySalesDto> dayOfWeekSales =
                orderRepository.findDayOfWeekSales(storeId, startDate, endDate)
                .stream()
                .map(row ->
                    new DaySalesDto(
                        ((Number)row[0]).intValue(),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();





        List<TimeOrderDto> hourlyOrderCount =
                orderRepository.findHourlyOrderCount(storeId, startDate, endDate)
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
                productRepository.findTopSellingProducts(storeId, startDate, endDate)
                .stream()
                .map(row ->
                    new ProductRankDto(
                        String.valueOf(row[0]),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();






        List<ProductSalesDto> productSales =
                productRepository.findProductSales(storeId, startDate, endDate)
                .stream()
                .map(row ->
                    new ProductSalesDto(
                        String.valueOf(row[0]),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();






        List<CategorySalesDto> categorySales =
                productRepository.findCategorySales(storeId, startDate, endDate)
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
         * =========================
         */



        List<ExpensePeriodDto> expensePeriod =
                expenseRepository.findDailyExpense(storeId, startDate, endDate)
                .stream()
                .map(row ->
                    new ExpensePeriodDto(
                        String.valueOf(row[0]),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();






        List<ExpenseCategoryDto> expenseCategory =
                expenseRepository.findExpenseByCategory(storeId, startDate, endDate)
                .stream()
                .map(row ->
                    new ExpenseCategoryDto(
                        String.valueOf(row[0]),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();






        List<ExpensePaymentDto> expensePayment =
                expenseRepository.findExpenseByPaymentMethod(storeId, startDate, endDate)
                .stream()
                .map(row ->
                    new ExpensePaymentDto(
                        String.valueOf(row[0]),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();







        /*
         * =========================
         * 최종 Response
         * =========================
         */


        return BranchStatisticsResponse.builder()


                .totalSales(sales)

                .orderCount(orderCount)

                .averageOrderPrice(averageOrderPrice)

                .totalExpense(expense)

                .profit(
                    sales - expense
                )
                
                .totalPaymentAmount(totalPaymentAmount)

                .couponDiscountAmount(couponDiscountAmount)

                .pointAmount(pointAmount)

                .finalPaymentAmount(finalPaymentAmount)



                .dailySales(dailySales)

                .monthlySales(monthlySales)

                .yearlySales(yearlySales)



                .hourlySales(hourlySales)

                .dayOfWeekSales(dayOfWeekSales)

                .hourlyOrderCount(hourlyOrderCount)



                .topProducts(topProducts)

                .productSales(productSales)

                .categorySales(categorySales)



                .canceledOrders(canceledOrders)

                .cancelRate(cancelRate)



                .expensePeriod(expensePeriod)

                .expenseCategory(expenseCategory)

                .expensePayment(expensePayment)


                .build();

    }




    /*
     * null 방지
     */
    private Integer safeInteger(Integer value){

        return value == null ? 0 : value;

    }
    
    public DashboardStatisticsDto getDashboard(
            Integer storeId
    ){


        Integer todaySales =
                safeInteger(
                    orderRepository.findTodaySales(storeId)
                );


        Integer todayOrders =
                safeInteger(
                    orderRepository.countTodayOrders(storeId)
                );



        List<TimeSalesDto> hourlySales =
                orderRepository.findTodayHourlySales(storeId)
                .stream()
                .map(row ->
                    new TimeSalesDto(
                        ((Number)row[0]).intValue(),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();



        List<CategorySalesDto> categorySales =
                productRepository.findCategorySales(storeId)
                .stream()
                .map(row ->
                    new CategorySalesDto(
                        String.valueOf(row[0]),
                        ((Number)row[1]).intValue()
                    )
                )
                .toList();



        return DashboardStatisticsDto.builder()

                .todaySales(todaySales)

                .todayOrderCount(todayOrders)

                .todayHourlySales(hourlySales)

                .categorySales(categorySales)

                .build();

    }
    
    
    
    


}