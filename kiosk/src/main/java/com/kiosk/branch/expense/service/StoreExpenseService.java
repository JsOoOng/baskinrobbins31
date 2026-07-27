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

/**
 * [코드 흐름 안내] StoreExpenseService
 *
 * <p>역할: 지점 운영의 지출 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> BranchStoreExpenseRepository, StoreRepository, EmployeeRepository, StaffRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
public class StoreExpenseService {


    private final BranchStoreExpenseRepository storeExpenseRepository;

    private final StoreRepository storeRepository;

    private final EmployeeRepository employeeRepository;

    private final StaffRepository staffRepository;



    @Transactional
    /**
     * [메서드 흐름] createExpense
     * Controller 또는 상위 서비스에서 호출되어 BranchStoreExpenseRepository, StoreRepository, EmployeeRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
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
    /**
     * [메서드 흐름] getExpenseHistory
     * Controller 또는 상위 서비스에서 호출되어 BranchStoreExpenseRepository, StoreRepository, EmployeeRepository 등을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<ExpenseHistoryResponseDTO> getExpenseHistory(
            Integer storeId
    ) {

        return storeExpenseRepository.findExpenseHistory(storeId);

    }

}