package com.kiosk.branch.expense.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kiosk.branch.expense.dto.ExpenseHistoryResponseDTO;
import com.kiosk.entity.StoreExpense;

/**
 * [코드 흐름 안내] BranchStoreExpenseRepository
 *
 * <p>역할: 지점 운영의 지출 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(STORE_EXPENSES) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface BranchStoreExpenseRepository
        extends JpaRepository<StoreExpense, Integer> {

    @Query("""
        SELECT new com.kiosk.branch.expense.dto.ExpenseHistoryResponseDTO(
            e.description,
            e.expenseCategory,
            e.paymentMethod
        )
        FROM StoreExpense e
        WHERE e.store.id = :storeId
        AND e.createdAt = (
            SELECT MAX(se.createdAt)
            FROM StoreExpense se
            WHERE se.store.id = e.store.id
            AND se.description = e.description
        )
        ORDER BY e.description
    """)
    List<ExpenseHistoryResponseDTO> findExpenseHistory(Integer storeId);

}