package com.kiosk.branch.statistics.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.OrderItem;


/**
 * [코드 흐름 안내] StatisticsProductRepository
 *
 * <p>역할: 지점 운영의 상품·메뉴 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(ORDER_ITEMS) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Repository
public interface StatisticsProductRepository
extends JpaRepository<OrderItem, Integer> {



/*
 * ==================================
 * 베스트 판매 상품 TOP N
 * 판매 수량 기준
 * ==================================
 */
@Query("""
    SELECT

        oi.product.productName,

        SUM(oi.quantity)


    FROM OrderItem oi


    JOIN oi.order o


    WHERE o.store.id = :storeId


    AND o.orderStatus = 'COMPLETED'


    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime


    GROUP BY

        oi.product.id,

        oi.product.productName


    ORDER BY

        SUM(oi.quantity) DESC

""")
List<Object[]> findTopSellingProducts(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

);






/*
 * ==================================
 * 상품별 매출
 * ==================================
 */
@Query("""
    SELECT

        oi.product.productName,

        SUM(
            oi.quantity * oi.unitPrice
        )


    FROM OrderItem oi


    JOIN oi.order o


    WHERE o.store.id = :storeId


    AND o.orderStatus = 'COMPLETED'


    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime


    GROUP BY

        oi.product.id,

        oi.product.productName


    ORDER BY

        SUM(
            oi.quantity * oi.unitPrice
        )
        DESC

""")
List<Object[]> findProductSales(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

);








/*
 * ==================================
 * 카테고리별 매출
 * ==================================
 */
@Query("""
    SELECT

        oi.product.category.categoryName,

        SUM(
            oi.quantity * oi.unitPrice
        )


    FROM OrderItem oi


    JOIN oi.order o


    WHERE o.store.id = :storeId


    AND o.orderStatus = 'COMPLETED'


    AND o.createdAt
    BETWEEN :startDateTime AND :endDateTime


    GROUP BY

        oi.product.category.id,

        oi.product.category.categoryName


    ORDER BY

        SUM(
            oi.quantity * oi.unitPrice
        )
        DESC

""")
List<Object[]> findCategorySales(

        @Param("storeId")
        Integer storeId,

        @Param("startDateTime")
        LocalDateTime startDateTime,

        @Param("endDateTime")
        LocalDateTime endDateTime

);






/*
 * ==================================
 * 대시보드용 카테고리 매출
 *
 * 전체 누적
 * ==================================
 */
@Query("""
    SELECT

        oi.product.category.categoryName,

        SUM(
            oi.quantity * oi.unitPrice
        )


    FROM OrderItem oi


    JOIN oi.order o


    WHERE o.store.id = :storeId


    AND o.orderStatus = 'COMPLETED'


    GROUP BY

        oi.product.category.id,

        oi.product.category.categoryName


    ORDER BY

        SUM(
            oi.quantity * oi.unitPrice
        )
        DESC

""")
List<Object[]> findCategorySales(

        @Param("storeId")
        Integer storeId

);



}