package com.kiosk.branch.parttime.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.parttime.dto.SalaryPaymentRequestDTO;
import com.kiosk.branch.parttime.dto.SalaryResponseDTO;
import com.kiosk.branch.parttime.repository.EmployeeRepository;
import com.kiosk.branch.parttime.repository.StaffRepository;
import com.kiosk.branch.parttime.repository.StoreExpenseRepository;
import com.kiosk.branch.parttime.repository.WorkHistoryRepository;
import com.kiosk.entity.Employee;
import com.kiosk.entity.Staff;
import com.kiosk.entity.StoreExpense;
import com.kiosk.entity.WorkHistory;
import com.kiosk.entity.enums.ExpenseCategory;
import com.kiosk.entity.enums.ExpensePaymentMethod;
import com.kiosk.entity.enums.WorkDay;
import com.kiosk.entity.enums.WorkStatus;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
@Transactional
public class BranchSalaryService {


    private final StaffRepository staffRepository;

    private final WorkHistoryRepository workHistoryRepository;

    private final EmployeeRepository employeeRepository;

    private final StoreExpenseRepository storeExpenseRepository;



    /*
     * 월 급여 계산
     */
    @Transactional(readOnly = true)
    public SalaryResponseDTO calculateSalary(
            Integer staffId,
            Integer year,
            Integer month
    ) {


        Staff staff =
                staffRepository.findById(staffId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "직원을 찾을 수 없습니다."
                        )
                );


        YearMonth yearMonth =
                YearMonth.of(year, month);


        LocalDate startDate =
                yearMonth.atDay(1);


        LocalDate endDate =
                yearMonth.atEndOfMonth();



        List<WorkHistory> histories =
                workHistoryRepository
                .findByStaffAndWorkDateBetweenOrderByWorkDateAsc(
                        staff,
                        startDate,
                        endDate
                );



        double totalHours = 0;


        int weekdayPay = 0;

        int weekendPay = 0;

        int holidayPay = 0;



        int hourlyWage =
                staff.getHourlyWage();



        for(WorkHistory history : histories){



            /*
             * 급여 계산 대상
             *
             * COMPLETED
             * LATE
             */
            if(
                history.getWorkStatus()
                != WorkStatus.COMPLETED

                &&
                history.getWorkStatus()
                != WorkStatus.LATE
            ){

                continue;

            }



            long minutes =
                    Duration.between(
                            history.getStartTime(),
                            history.getEndTime()
                    )
                    .toMinutes();



            double hours =
                    minutes / 60.0;



            totalHours += hours;



            int pay =
                    (int)
                    Math.round(
                            hours * hourlyWage
                    );



            /*
             * 공휴일
             */
            if(
                Boolean.TRUE.equals(
                        history.getIsHoliday()
                )
            ){


                pay =
                (int)
                Math.round(
                    hours
                    * hourlyWage
                    * 1.5
                );


                holidayPay += pay;


            }


            /*
             * 토, 일
             */
            else if(
                history.getDayOfWeek()
                == WorkDay.SAT

                ||

                history.getDayOfWeek()
                == WorkDay.SUN
            ){


                pay =
                (int)
                Math.round(
                    hours
                    * hourlyWage
                    * 1.5
                );


                weekendPay += pay;


            }


            /*
             * 평일
             */
            else {


                weekdayPay += pay;


            }


        }



        boolean paid =
                storeExpenseRepository
                .existsByStaffAndExpenseCategoryAndExpenseDateBetween(
                        staff,
                        ExpenseCategory.LABOR,
                        startDate,
                        endDate
                );



        return SalaryResponseDTO.builder()

                .staffId(staff.getId())

                .staffName(staff.getName())

                .hourlyWage(hourlyWage)

                .totalHours(totalHours)

                .weekdayPay(weekdayPay)

                .weekendPay(weekendPay)

                .holidayPay(holidayPay)

                .totalSalary(
                        weekdayPay
                        +
                        weekendPay
                        +
                        holidayPay
                )

                .paid(paid)

                .build();

    }





    /*
     * 급여 지급 처리
     *
     * 계산된 급여를 지출 내역으로 저장
     */
    public void paySalary(
            Integer staffId,
            SalaryPaymentRequestDTO requestDTO
    ){


        Staff staff =
                staffRepository.findById(staffId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "직원을 찾을 수 없습니다."
                        )
                );



        YearMonth yearMonth =
                YearMonth.of(
                        requestDTO.getYear(),
                        requestDTO.getMonth()
                );


        LocalDate startDate =
                yearMonth.atDay(1);


        LocalDate endDate =
                yearMonth.atEndOfMonth();



        boolean alreadyPaid =
                storeExpenseRepository
                .existsByStaffAndExpenseCategoryAndExpenseDateBetween(
                        staff,
                        ExpenseCategory.LABOR,
                        startDate,
                        endDate
                );



        if(alreadyPaid){

            throw new IllegalArgumentException(
                    "해당 월 급여는 이미 지급되었습니다."
            );

        }



        Employee employee =
                employeeRepository.findById(
                        requestDTO.getEmployeeId()
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "등록자를 찾을 수 없습니다."
                        )
                );



        SalaryResponseDTO salary =
                calculateSalary(
                        staffId,
                        requestDTO.getYear(),
                        requestDTO.getMonth()
                );



        if(
            salary.getTotalSalary() == null
            ||
            salary.getTotalSalary() <= 0
        ){

            throw new IllegalArgumentException(
                    "지급할 급여가 없습니다."
            );

        }



        StoreExpense expense =
                StoreExpense.builder()

                .store(
                        staff.getStore()
                )

                .employee(
                        employee
                )

                .staff(
                        staff
                )

                .expenseCategory(
                        ExpenseCategory.LABOR
                )

                .paymentMethod(
                        ExpensePaymentMethod.valueOf(
                                requestDTO.getPaymentMethod()
                        )
                )

                /*
                 * 실제 지급일이 아니라
                 * 급여 대상 월 저장
                 */
                .expenseDate(
                		LocalDate.now()
                )

                .amount(
                        salary.getTotalSalary()
                )

                .description(
                        requestDTO.getDescription()
                )

                .build();



        storeExpenseRepository.save(expense);

    }

}