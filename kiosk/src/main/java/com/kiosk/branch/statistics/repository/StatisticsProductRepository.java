package com.kiosk.branch.statistics.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.OrderItem;


@Repository
public interface StatisticsProductRepository
extends JpaRepository<OrderItem, Integer>{



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

        GROUP BY
            oi.product.id,
            oi.product.productName

        ORDER BY
            SUM(oi.quantity) DESC

    """)
    List<Object[]> findTopSellingProducts(
            Integer storeId
    );





    /*
     * ==================================
     * 상품별 매출
     * ==================================
     */
    @Query("""
        SELECT
            oi.product.productName,
            SUM(oi.quantity * oi.unitPrice)

        FROM OrderItem oi

        JOIN oi.order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'COMPLETED'

        GROUP BY
            oi.product.id,
            oi.product.productName

        ORDER BY
            SUM(oi.quantity * oi.unitPrice) DESC

    """)
    List<Object[]> findProductSales(
            Integer storeId
    );





    /*
     * ==================================
     * 카테고리별 매출
     * ==================================
     */
    @Query("""
        SELECT
            oi.product.category.categoryName,
            SUM(oi.quantity * oi.unitPrice)

        FROM OrderItem oi

        JOIN oi.order o

        WHERE o.store.id = :storeId

        AND o.orderStatus = 'COMPLETED'

        GROUP BY
            oi.product.category.id,
            oi.product.category.categoryName

        ORDER BY
            SUM(oi.quantity * oi.unitPrice) DESC

    """)
    List<Object[]> findCategorySales(
            Integer storeId
    );


}