package com.kiosk.branch.expense.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.expense.dto.StoreExpenseRequestDTO;
import com.kiosk.branch.expense.repository.BranchStoreExpenseRepository;
import com.kiosk.branch.kiosk.repository.StoreRepository;
import com.kiosk.branch.parttime.repository.EmployeeRepository;
import com.kiosk.branch.parttime.repository.StaffRepository;
import com.kiosk.entity.Employee;
import com.kiosk.entity.Staff;
import com.kiosk.entity.Store;
import com.kiosk.entity.StoreExpense;

import lombok.RequiredArgsConstructor;
import java.util.List;

import com.kiosk.branch.expense.dto.ExpenseHistoryResponseDTO;

@Service
@RequiredArgsConstructor
public class StoreExpenseService {


    private final BranchStoreExpenseRepository storeExpenseRepository;

    private final StoreRepository storeRepository;

    private final EmployeeRepository employeeRepository;

    private final StaffRepository staffRepository;



    @Transactional
    public void createExpense(StoreExpenseRequestDTO dto) {


        Store store =
                storeRepository.findById(dto.getStoreId())
                .orElseThrow(
                        () -> new RuntimeException("매장을 찾을 수 없습니다.")
                );


        Employee employee =
                employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(
                        () -> new RuntimeException("등록자를 찾을 수 없습니다.")
                );


        Staff staff = null;


        // 급여 지출일 경우만 Staff 연결
        if(dto.getStaffId() != null) {

            staff =
                    staffRepository.findById(dto.getStaffId())
                    .orElseThrow(
                            () -> new RuntimeException("직원을 찾을 수 없습니다.")
                    );
        }



        StoreExpense expense =
                StoreExpense.builder()

                .store(store)

                .employee(employee)

                .staff(staff)

                .expenseCategory(
                        dto.getExpenseCategory()
                )

                .paymentMethod(
                        dto.getPaymentMethod()
                )

                .expenseDate(
                        dto.getExpenseDate()
                )

                .amount(
                        dto.getAmount()
                )

                .description(
                        dto.getDescription()
                )

                .receiptUrl(
                        dto.getReceiptUrl()
                )

                .build();



        storeExpenseRepository.save(expense);

    }
    
    /*
     * 지출 자동완성 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ExpenseHistoryResponseDTO> getExpenseHistory(
            Integer storeId
    ) {

        return storeExpenseRepository.findExpenseHistory(storeId);

    }

}