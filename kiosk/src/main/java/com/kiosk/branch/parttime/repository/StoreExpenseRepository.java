package com.kiosk.branch.parttime.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kiosk.entity.Staff;
import com.kiosk.entity.StoreExpense;
import com.kiosk.entity.enums.ExpenseCategory;


/**
 * [코드 흐름 안내] StoreExpenseRepository
 *
 * <p>역할: 지점 운영의 지출 데이터를 조회하거나 저장하는 DB 접근 계층이다.</p>
 * <p>호출 흐름: Service -> 이 Repository(STORE_EXPENSES) -> MySQL -> 조회/변경 결과 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
public interface StoreExpenseRepository 
        extends JpaRepository<StoreExpense, Integer>{



    boolean existsByStaffAndExpenseCategoryAndExpenseDateBetween(
            Staff staff,
            ExpenseCategory expenseCategory,
            LocalDate startDate,
            LocalDate endDate
    );


}