package com.kiosk.branch.statistics.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Order;


@Repository
public interface StatisticsOrderRepository
        extends JpaRepository<Order, Integer> {



    /*
     * =====================================
     * 총 매출
     * COMPLETED 주문만 집계
     * =====================================
     */
    @Query("""
        SELECT COALESCE(SUM(o.totalPrice),0)

        FROM Order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'COMPLETED'
    """)
    Integer findTotalSales(
            @Param("storeId") Integer storeId
    );





    /*
     * =====================================
     * 완료 주문 건수
     * =====================================
     */
    @Query("""
        SELECT COUNT(o)

        FROM Order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'COMPLETED'
    """)
    Integer countCompletedOrders(
            @Param("storeId") Integer storeId
    );





    /*
     * =====================================
     * 전체 주문 건수
     * 취소율 계산용
     * =====================================
     */
    @Query("""
        SELECT COUNT(o)

        FROM Order o

        WHERE o.store.id = :storeId
    """)
    Integer countAllOrders(
            @Param("storeId") Integer storeId
    );





    /*
     * =====================================
     * 일별 매출
     * =====================================
     */
    @Query("""
        SELECT 
            FUNCTION('DATE',o.createdAt),
            SUM(o.totalPrice)

        FROM Order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'COMPLETED'

        GROUP BY FUNCTION('DATE',o.createdAt)

        ORDER BY FUNCTION('DATE',o.createdAt)
    """)
    List<Object[]> findDailySales(
            @Param("storeId") Integer storeId
    );





    /*
     * =====================================
     * 월별 매출
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('YEAR',o.createdAt),
            FUNCTION('MONTH',o.createdAt),
            SUM(o.totalPrice)

        FROM Order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'COMPLETED'

        GROUP BY
            FUNCTION('YEAR',o.createdAt),
            FUNCTION('MONTH',o.createdAt)

        ORDER BY
            FUNCTION('YEAR',o.createdAt),
            FUNCTION('MONTH',o.createdAt)

    """)
    List<Object[]> findMonthlySales(
            @Param("storeId") Integer storeId
    );





    /*
     * =====================================
     * 연별 매출
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('YEAR',o.createdAt),
            SUM(o.totalPrice)

        FROM Order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'COMPLETED'

        GROUP BY FUNCTION('YEAR',o.createdAt)

        ORDER BY FUNCTION('YEAR',o.createdAt)

    """)
    List<Object[]> findYearlySales(
            @Param("storeId") Integer storeId
    );





    /*
     * =====================================
     * 시간대별 매출
     * 최고/최저 매출 시간 분석
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('HOUR',o.createdAt),
            SUM(o.totalPrice)

        FROM Order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'COMPLETED'

        GROUP BY FUNCTION('HOUR',o.createdAt)

        ORDER BY FUNCTION('HOUR',o.createdAt)

    """)
    List<Object[]> findHourlySales(
            @Param("storeId") Integer storeId
    );





    /*
     * =====================================
     * 요일별 매출
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('DAYOFWEEK',o.createdAt),
            SUM(o.totalPrice)

        FROM Order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'COMPLETED'

        GROUP BY FUNCTION('DAYOFWEEK',o.createdAt)

        ORDER BY FUNCTION('DAYOFWEEK',o.createdAt)

    """)
    List<Object[]> findDayOfWeekSales(
            @Param("storeId") Integer storeId
    );





    /*
     * =====================================
     * 주문 취소 건수
     * =====================================
     */
    @Query("""
        SELECT COUNT(o)

        FROM Order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'CANCELED'

    """)
    Integer countCanceledOrders(
            @Param("storeId") Integer storeId
    );





    /*
     * =====================================
     * 시간대별 주문량
     * 직원 배치 / 피크타임 분석용
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('HOUR',o.createdAt),
            COUNT(o)

        FROM Order o

        WHERE o.store.id = :storeId

        GROUP BY FUNCTION('HOUR',o.createdAt)

        ORDER BY FUNCTION('HOUR',o.createdAt)

    """)
    List<Object[]> findHourlyOrderCount(
            @Param("storeId") Integer storeId
    );



}