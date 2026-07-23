package com.kiosk.branch.statistics.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.Order;


/**
 * [코드 흐름 안내] StatisticsOrderRepository
 *
 * <p>역할: 지점 운영의 주문 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(ORDERS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime

""")
Integer findTotalSales(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime

""")
Integer countCompletedOrders(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime

""")
Integer countAllOrders(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime


    GROUP BY FUNCTION('DATE', o.createdAt)

    ORDER BY FUNCTION('DATE', o.createdAt)

""")
List<Object[]> findDailySales(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime


    GROUP BY

        FUNCTION('YEAR', o.createdAt),

        FUNCTION('MONTH', o.createdAt)


    ORDER BY

        FUNCTION('YEAR', o.createdAt),

        FUNCTION('MONTH', o.createdAt)

""")
List<Object[]> findMonthlySales(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime


    GROUP BY FUNCTION('YEAR', o.createdAt)


    ORDER BY FUNCTION('YEAR', o.createdAt)

""")
List<Object[]> findYearlySales(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime


    GROUP BY FUNCTION('HOUR', o.createdAt)


    ORDER BY FUNCTION('HOUR', o.createdAt)

""")
List<Object[]> findHourlySales(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime


    GROUP BY FUNCTION('DAYOFWEEK', o.createdAt)


    ORDER BY FUNCTION('DAYOFWEEK', o.createdAt)

""")
List<Object[]> findDayOfWeekSales(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime

""")
Integer countCanceledOrders(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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


    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime


    GROUP BY FUNCTION('HOUR', o.createdAt)


    ORDER BY FUNCTION('HOUR', o.createdAt)

""")
List<Object[]> findHourlyOrderCount(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

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

    AND FUNCTION('DATE', o.createdAt)
    = CURRENT_DATE

""")
Integer findTodaySales(
        @Param("storeId")
        Integer storeId
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

    AND FUNCTION('DATE', o.createdAt)
    = CURRENT_DATE

""")
Integer countTodayOrders(
        @Param("storeId")
        Integer storeId
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

    AND FUNCTION('DATE', o.createdAt)
    = CURRENT_DATE


    GROUP BY FUNCTION('HOUR', o.createdAt)


    ORDER BY FUNCTION('HOUR', o.createdAt)

""")
List<Object[]> findTodayHourlySales(
        @Param("storeId")
        Integer storeId
);



/*
 * =====================================
 * 총 결제금액
 * =====================================
 */
@Query("""
    SELECT COALESCE(SUM(p.baseAmount),0)

    FROM Order o

    JOIN o.payment p


    WHERE o.store.id = :storeId

    AND o.orderStatus = 'COMPLETED'

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime

""")
Integer findTotalPaymentAmount(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

);



/*
 * =====================================
 * 총 쿠폰 할인금액
 * =====================================
 */
@Query("""
    SELECT COALESCE(SUM(p.couponDiscount),0)

    FROM Order o

    JOIN o.payment p


    WHERE o.store.id = :storeId

    AND o.orderStatus = 'COMPLETED'

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime

""")
Integer findCouponDiscountAmount(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

);



/*
 * =====================================
 * 총 포인트 사용금액
 * =====================================
 */
@Query("""
    SELECT COALESCE(SUM(p.pointUsed),0)

    FROM Order o

    JOIN o.payment p


    WHERE o.store.id = :storeId

    AND o.orderStatus = 'COMPLETED'

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime

""")
Integer findPointAmount(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

);



/*
 * =====================================
 * 총 최종 결제금액
 * =====================================
 */
@Query("""
    SELECT COALESCE(SUM(p.finalAmount),0)

    FROM Order o

    JOIN o.payment p


    WHERE o.store.id = :storeId

    AND o.orderStatus = 'COMPLETED'

    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime

""")
Integer findFinalPaymentAmount(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

);

}