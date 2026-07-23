package com.kiosk.branch.expense.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kiosk.branch.expense.dto.ExpenseHistoryResponseDTO;
import com.kiosk.entity.StoreExpense;

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