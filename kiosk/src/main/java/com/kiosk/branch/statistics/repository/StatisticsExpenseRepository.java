package com.kiosk.branch.statistics.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.StoreExpense;



@Repository
public interface StatisticsExpenseRepository
extends JpaRepository<StoreExpense, Integer>{



    /*
     * =====================================
     * 총 지출
     * =====================================
     */
    @Query("""
        SELECT COALESCE(SUM(e.amount),0)

        FROM StoreExpense e

        WHERE e.store.id = :storeId
    """)
    Integer findTotalExpense(
            @Param("storeId") Integer storeId
    );






    /*
     * =====================================
     * 일별 지출
     * =====================================
     */
    @Query("""
        SELECT
            e.expenseDate,
            SUM(e.amount)

        FROM StoreExpense e

        WHERE e.store.id = :storeId

        GROUP BY e.expenseDate

        ORDER BY e.expenseDate

    """)
    List<Object[]> findDailyExpense(
            @Param("storeId") Integer storeId
    );






    /*
     * =====================================
     * 지출 카테고리별 통계
     *
     * INGREDIENTS
     * LABOR
     * UTILITY
     * RENT
     * ETC ...
     * =====================================
     */
    @Query("""
        SELECT
            e.expenseCategory,
            SUM(e.amount)

        FROM StoreExpense e

        WHERE e.store.id = :storeId

        GROUP BY e.expenseCategory

    """)
    List<Object[]> findExpenseByCategory(
            @Param("storeId") Integer storeId
    );






    /*
     * =====================================
     * 지출 결제 방식 분석
     *
     * CARD
     * CASH
     * TRANSFER
     * =====================================
     */
    @Query("""
        SELECT
            e.paymentMethod,
            SUM(e.amount)

        FROM StoreExpense e

        WHERE e.store.id = :storeId

        GROUP BY e.paymentMethod

    """)
    List<Object[]> findExpenseByPaymentMethod(
            @Param("storeId") Integer storeId
    );



}