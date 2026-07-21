package com.kiosk.branch.expense.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.StoreExpense;

public interface BranchStoreExpenseRepository 
        extends JpaRepository<StoreExpense, Integer> {

}