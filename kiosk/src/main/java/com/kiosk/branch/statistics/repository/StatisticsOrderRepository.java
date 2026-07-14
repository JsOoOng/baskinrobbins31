package com.kiosk.branch.statistics.repository;

import java.time.LocalDate;
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
     * =====================================
     */
    @Query("""
        SELECT COALESCE(SUM(p.finalAmount),0)
        FROM Order o
        JOIN o.payment p
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'COMPLETED'
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
    """)
    Integer findTotalSales(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
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
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
    """)
    Integer countCompletedOrders(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * =====================================
     * 전체 주문 건수
     * =====================================
     */
    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.store.id = :storeId
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
    """)
    Integer countAllOrders(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * =====================================
     * 일별 매출
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('DATE', o.createdAt),
            SUM(p.finalAmount)
        FROM Order o
        JOIN o.payment p
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'COMPLETED'
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('DATE', o.createdAt)
        ORDER BY FUNCTION('DATE', o.createdAt)
    """)
    List<Object[]> findDailySales(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * =====================================
     * 월별 매출
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('YEAR', o.createdAt),
            FUNCTION('MONTH', o.createdAt),
            SUM(p.finalAmount)
        FROM Order o
        JOIN o.payment p
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'COMPLETED'
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
        GROUP BY
            FUNCTION('YEAR', o.createdAt),
            FUNCTION('MONTH', o.createdAt)
        ORDER BY
            FUNCTION('YEAR', o.createdAt),
            FUNCTION('MONTH', o.createdAt)
    """)
    List<Object[]> findMonthlySales(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * =====================================
     * 연별 매출
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('YEAR', o.createdAt),
            SUM(p.finalAmount)
        FROM Order o
        JOIN o.payment p
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'COMPLETED'
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('YEAR', o.createdAt)
        ORDER BY FUNCTION('YEAR', o.createdAt)
    """)
    List<Object[]> findYearlySales(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * =====================================
     * 시간대별 매출
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('HOUR', o.createdAt),
            SUM(p.finalAmount)
        FROM Order o
        JOIN o.payment p
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'COMPLETED'
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('HOUR', o.createdAt)
        ORDER BY FUNCTION('HOUR', o.createdAt)
    """)
    List<Object[]> findHourlySales(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * =====================================
     * 요일별 매출
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('DAYOFWEEK', o.createdAt),
            SUM(p.finalAmount)
        FROM Order o
        JOIN o.payment p
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'COMPLETED'
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('DAYOFWEEK', o.createdAt)
        ORDER BY FUNCTION('DAYOFWEEK', o.createdAt)
    """)
    List<Object[]> findDayOfWeekSales(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * =====================================
     * 취소 주문 건수
     * =====================================
     */
    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'CANCELED'
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
    """)
    Integer countCanceledOrders(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * =====================================
     * 시간대별 주문량
     * =====================================
     */
    @Query("""
        SELECT
            FUNCTION('HOUR', o.createdAt),
            COUNT(o)
        FROM Order o
        WHERE o.store.id = :storeId
        AND FUNCTION('DATE', o.createdAt)
        BETWEEN :startDate AND :endDate
        GROUP BY FUNCTION('HOUR', o.createdAt)
        ORDER BY FUNCTION('HOUR', o.createdAt)
    """)
    List<Object[]> findHourlyOrderCount(
            @Param("storeId") Integer storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /*
     * ===========================
     * 오늘 매출
     * ===========================
     */
    @Query("""
        SELECT COALESCE(SUM(p.finalAmount),0)
        FROM Order o
        JOIN o.payment p
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'COMPLETED'
        AND FUNCTION('DATE', o.createdAt) = CURRENT_DATE
    """)
    Integer findTodaySales(
            @Param("storeId") Integer storeId
    );

    /*
     * ===========================
     * 오늘 주문 수
     * ===========================
     */
    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'COMPLETED'
        AND FUNCTION('DATE', o.createdAt) = CURRENT_DATE
    """)
    Integer countTodayOrders(
            @Param("storeId") Integer storeId
    );

    /*
     * ===========================
     * 오늘 시간대별 매출
     * ===========================
     */
    @Query("""
        SELECT
            FUNCTION('HOUR', o.createdAt),
            SUM(p.finalAmount)
        FROM Order o
        JOIN o.payment p
        WHERE o.store.id = :storeId
        AND o.orderStatus = 'COMPLETED'
        AND FUNCTION('DATE', o.createdAt) = CURRENT_DATE
        GROUP BY FUNCTION('HOUR', o.createdAt)
        ORDER BY FUNCTION('HOUR', o.createdAt)
    """)
    List<Object[]> findTodayHourlySales(
            @Param("storeId") Integer storeId
    );
}