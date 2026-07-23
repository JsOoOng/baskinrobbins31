package com.kiosk.branch.statistics.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kiosk.entity.StoreExpense;


/**
 * [코드 흐름 안내] StatisticsExpenseRepository
 *
 * <p>역할: 지점 운영의 지출 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(STORE_EXPENSES) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
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

        AND e.expenseDate
        BETWEEN :startDate AND :endDate

    """)
    Integer findTotalExpense(

            @Param("storeId") Integer storeId,

            @Param("startDate") LocalDate startDate,

            @Param("endDate") LocalDate endDate

    );










    /*
     * =====================================
     * 기간별 지출
     * 일별 지출
     * =====================================
     */
    @Query("""
        SELECT
            e.expenseDate,
            SUM(e.amount)

        FROM StoreExpense e

        WHERE e.store.id = :storeId

        AND e.expenseDate
        BETWEEN :startDate AND :endDate


        GROUP BY e.expenseDate

        ORDER BY e.expenseDate

    """)
    List<Object[]> findDailyExpense(

            @Param("storeId") Integer storeId,

            @Param("startDate") LocalDate startDate,

            @Param("endDate") LocalDate endDate

    );









    /*
     * =====================================
     * 지출 카테고리별 통계
     *
     * INGREDIENTS
     * LABOR
     * UTILITY
     * RENT
     * ETC
     *
     * =====================================
     */
    @Query("""
        SELECT
            e.expenseCategory,
            SUM(e.amount)

        FROM StoreExpense e


        WHERE e.store.id = :storeId


        AND e.expenseDate
        BETWEEN :startDate AND :endDate


        GROUP BY e.expenseCategory

    """)
    List<Object[]> findExpenseByCategory(

            @Param("storeId") Integer storeId,

            @Param("startDate") LocalDate startDate,

            @Param("endDate") LocalDate endDate

    );









    /*
     * =====================================
     * 지출 결제 방식 분석
     *
     * CARD
     * CASH
     * TRANSFER
     *
     * =====================================
     */
    @Query("""
        SELECT
            e.paymentMethod,
            SUM(e.amount)

        FROM StoreExpense e


        WHERE e.store.id = :storeId


        AND e.expenseDate
        BETWEEN :startDate AND :endDate


        GROUP BY e.paymentMethod

    """)
    List<Object[]> findExpenseByPaymentMethod(

            @Param("storeId") Integer storeId,

            @Param("startDate") LocalDate startDate,

            @Param("endDate") LocalDate endDate

    );
    
    @Query("""
    	    SELECT e
    	    FROM StoreExpense e
    	    WHERE e.store.id = :storeId
    	    AND e.expenseDate BETWEEN :startDate AND :endDate
    	    ORDER BY e.expenseDate DESC
    	""")
    	List<StoreExpense> findExpenseDetail(
    	        Integer storeId,
    	        LocalDate startDate,
    	        LocalDate endDate
    	);


}