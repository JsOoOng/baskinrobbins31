package com.kiosk.branch.parttime.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.parttime.dto.SalaryResponseDTO;
import com.kiosk.branch.parttime.dto.WorkHistoryResponseDTO;
import com.kiosk.branch.parttime.dto.WorkHistoryUpdateRequestDTO;
import com.kiosk.branch.parttime.repository.StaffRepository;
import com.kiosk.branch.parttime.repository.WorkHistoryRepository;
import com.kiosk.entity.Staff;
import com.kiosk.entity.WorkHistory;
import com.kiosk.entity.enums.WorkDay;
import com.kiosk.entity.enums.WorkStatus;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] BranchWorkHistoryService
 *
 * <p>역할: 지점 운영의 근무 이력 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> WorkHistoryRepository, StaffRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchWorkHistoryService {

    private final WorkHistoryRepository workHistoryRepository;
    private final StaffRepository staffRepository;

    private Staff getStaff(
            Integer staffId
    ) {

        return staffRepository.findById(staffId)
                .orElseThrow(() ->
                        new IllegalArgumentException("직원을 찾을 수 없습니다."));

    }
    
    /**
     * [메서드 흐름] getMonthHistory
     * Controller 또는 상위 서비스에서 호출되어 WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<WorkHistoryResponseDTO> getMonthHistory(
            Integer staffId,
            Integer year,
            Integer month
    ) {

        Staff staff = getStaff(staffId);

        YearMonth yearMonth =
                YearMonth.of(year, month);

        LocalDate startDate =
                yearMonth.atDay(1);

        LocalDate endDate =
                yearMonth.atEndOfMonth();

        return workHistoryRepository
                .findByStaffAndWorkDateBetweenOrderByWorkDateAsc(
                        staff,
                        startDate,
                        endDate
                )
                .stream()
                .map(history ->
                        WorkHistoryResponseDTO.builder()
                                .historyId(history.getId())
                                .workDate(history.getWorkDate())
                                .dayOfWeek(history.getDayOfWeek())
                                .startTime(history.getStartTime())
                                .endTime(history.getEndTime())
                                .isHoliday(history.getIsHoliday())
                                .workStatus(history.getWorkStatus())
                                .build()
                )
                .toList();

    }
    
    @Transactional
    /**
     * [메서드 흐름] updateHistory
     * Controller 또는 상위 서비스에서 호출되어 WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void updateHistory(
            Integer historyId,
            WorkHistoryUpdateRequestDTO requestDTO
    ) {

        WorkHistory history =
                workHistoryRepository.findById(historyId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("근무 기록이 없습니다."));

        if (history.getWorkDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "지난 근무는 수정할 수 없습니다."
            );
        }

        history.update(
                requestDTO.getStartTime(),
                requestDTO.getEndTime(),
                requestDTO.getIsHoliday()
        );

    }
    
    @Transactional
    /**
     * [메서드 흐름] changeWorkStatus
     * Controller 또는 상위 서비스에서 호출되어 WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void changeWorkStatus(
            Integer historyId,
            WorkStatus workStatus
    ) {

        WorkHistory history =
                workHistoryRepository.findById(historyId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("근무 기록이 없습니다."));

        history.changeStatus(workStatus);

    }
    
    @Transactional
    /**
     * [메서드 흐름] changeHoliday
     * Controller 또는 상위 서비스에서 호출되어 WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void changeHoliday(
            Integer historyId,
            Boolean isHoliday
    ) {

        WorkHistory history =
                workHistoryRepository.findById(historyId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("근무 기록이 없습니다."));

        if (history.getWorkDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("지난 날짜는 수정할 수 없습니다.");
        }

        history.changeHoliday(isHoliday);

    }
    
    /**
     * [메서드 흐름] calculateSalary
     * Controller 또는 상위 서비스에서 호출되어 WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public SalaryResponseDTO calculateSalary(
            Integer staffId,
            Integer year,
            Integer month
    ) {

        Staff staff = getStaff(staffId);

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

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

        int hourlyWage = staff.getHourlyWage();

        for (WorkHistory history : histories) {

            // 근무 완료, 지각만 급여 계산
            if (history.getWorkStatus() != WorkStatus.COMPLETED
                    && history.getWorkStatus() != WorkStatus.LATE) {
                continue;
            }

            long minutes = Duration.between(
                    history.getStartTime(),
                    history.getEndTime()
            ).toMinutes();

            double hours = minutes / 60.0;

            totalHours += hours;

            // 기본 급여
            int pay = (int) Math.round(hours * hourlyWage);

            // 공휴일
            if (Boolean.TRUE.equals(history.getIsHoliday())) {

                pay = (int) Math.round(hours * hourlyWage * 1.5);

                holidayPay += pay;

            }

            // 토요일 또는 일요일
            else if (history.getDayOfWeek() == WorkDay.SAT
                    || history.getDayOfWeek() == WorkDay.SUN) {

                pay = (int) Math.round(hours * hourlyWage * 1.5);

                weekendPay += pay;

            }

            // 평일
            else {

                weekdayPay += pay;

            }

        }

        return SalaryResponseDTO.builder()
                .hourlyWage(hourlyWage)
                .totalHours(totalHours)
                .weekdayPay(weekdayPay)
                .weekendPay(weekendPay)
                .holidayPay(holidayPay)
                .totalSalary(
                        weekdayPay +
                        weekendPay +
                        holidayPay
                )
                .build();

    }
    
}