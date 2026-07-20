package com.kiosk.branch.parttime.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Staff;
import com.kiosk.entity.StoreExpense;
import com.kiosk.entity.enums.ExpenseCategory;


public interface StoreExpenseRepository 
        extends JpaRepository<StoreExpense, Integer>{



    boolean existsByStaffAndExpenseCategoryAndExpenseDateBetween(
            Staff staff,
            ExpenseCategory expenseCategory,
            LocalDate startDate,
            LocalDate endDate
    );


}