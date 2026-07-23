package com.kiosk.branch.parttime.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.parttime.dto.StoreWeekScheduleResponseDTO;
import com.kiosk.branch.parttime.dto.WeekScheduleRequestDTO;
import com.kiosk.branch.parttime.dto.WeekScheduleResponseDTO;
import com.kiosk.branch.parttime.dto.WeekScheduleSaveRequestDTO;
import com.kiosk.branch.parttime.repository.StaffRepository;
import com.kiosk.branch.parttime.repository.WeekScheduleRepository;
import com.kiosk.branch.parttime.repository.WorkHistoryRepository;
import com.kiosk.entity.Staff;
import com.kiosk.entity.WeekSchedule;
import com.kiosk.entity.WorkHistory;
import com.kiosk.entity.enums.StaffStatus;
import com.kiosk.entity.enums.WorkDay;

import lombok.RequiredArgsConstructor;


/**
 * [코드 흐름 안내] BranchWeekScheduleService
 *
 * <p>역할: 지점 운영의 근무 일정 업무 규칙과 상태 변경을 처리한다.</p>
 * <p>호출 흐름: Controller 호출 -> 이 서비스 -> WeekScheduleRepository, WorkHistoryRepository, StaffRepository -> Entity/DTO 변환 -> Controller 반환 순서로 동작한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchWeekScheduleService {


    private final WeekScheduleRepository weekScheduleRepository;

    private final WorkHistoryRepository workHistoryRepository;

    private final StaffRepository staffRepository;



    private Staff getStaff(Integer staffId) {

        return staffRepository.findById(staffId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "직원을 찾을 수 없습니다."
                        ));
    }



    /*
     * 최초 스케줄 저장
     */
    @Transactional
    /**
     * [메서드 흐름] saveWeekSchedule
     * Controller 또는 상위 서비스에서 호출되어 WeekScheduleRepository, WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void saveWeekSchedule(
            Integer staffId,
            WeekScheduleSaveRequestDTO requestDTO
    ) {

        Staff staff = getStaff(staffId);


        for (WeekScheduleRequestDTO dto 
                : requestDTO.getSchedules()) {


            // 시간 없는 요일은 저장하지 않음
            if(dto.getStartTime() == null ||
               dto.getEndTime() == null) {

                continue;
            }



            WeekSchedule schedule =
                    weekScheduleRepository
                    .findByStaffAndDayOfWeek(
                            staff,
                            dto.getDayOfWeek()
                    )
                    .orElse(null);



            // 기존 요일 수정
            if(schedule != null) {


                schedule.update(
                        dto.getStartTime(),
                        dto.getEndTime(),
                        dto.getIsHoliday()
                );


            }

            // 새로운 요일 추가
            else {


                weekScheduleRepository.save(

                    WeekSchedule.builder()
                        .staff(staff)
                        .dayOfWeek(dto.getDayOfWeek())
                        .startTime(dto.getStartTime())
                        .endTime(dto.getEndTime())
                        .isHoliday(dto.getIsHoliday())
                        .build()

                );

            }

        }


        generateWorkHistory(staff);

    }



    /*
     * 현재 월 근무이력 생성
     */
    private void generateWorkHistory(
            Staff staff
    ) {


        LocalDate today = LocalDate.now();


        LocalDate endDate =
                today.withDayOfMonth(
                        today.lengthOfMonth()
                );



        List<WeekSchedule> schedules =
                weekScheduleRepository
                        .findByStaffOrderByDayOfWeek(staff);



        for(LocalDate date = today;
            !date.isAfter(endDate);
            date = date.plusDays(1)) {



            WorkDay workDay =
                    WorkDay.valueOf(
                            date.getDayOfWeek()
                            .name()
                            .substring(0,3)
                    );



            for(WeekSchedule schedule : schedules) {



                if(schedule.getDayOfWeek()
                        != workDay) {

                    continue;

                }



                boolean exists =
                        workHistoryRepository
                        .existsByStaffAndWorkDate(
                                staff,
                                date
                        );



                if(exists) {

                    continue;

                }



                workHistoryRepository.save(

                        WorkHistory.builder()
                                .staff(staff)
                                .workDate(date)
                                .dayOfWeek(
                                        schedule.getDayOfWeek()
                                )
                                .startTime(
                                        schedule.getStartTime()
                                )
                                .endTime(
                                        schedule.getEndTime()
                                )
                                .isHoliday(
                                        schedule.getIsHoliday()
                                )
                                .build()

                );


            }


        }


    }




    /*
     * 스케줄 수정
     * 기존 요일 수정 + 신규 요일 추가
     */
    @Transactional
    /**
     * [메서드 흐름] updateWeekSchedule
     * Controller 또는 상위 서비스에서 호출되어 WeekScheduleRepository, WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void updateWeekSchedule(
            Integer staffId,
            WeekScheduleSaveRequestDTO requestDTO
    ) {


        Staff staff = getStaff(staffId);



        for(WeekScheduleRequestDTO dto 
                : requestDTO.getSchedules()) {



            // 입력 안한 요일은 무시
            if(dto.getStartTime() == null ||
               dto.getEndTime() == null) {

                continue;
            }



            WeekSchedule schedule =
                    weekScheduleRepository
                    .findByStaffAndDayOfWeek(
                            staff,
                            dto.getDayOfWeek()
                    )
                    .orElse(null);



            // 기존 요일 수정
            if(schedule != null) {


                schedule.update(
                        dto.getStartTime(),
                        dto.getEndTime(),
                        dto.getIsHoliday()
                );


            }


            // 새로운 요일 추가
            else {


                weekScheduleRepository.save(

                    WeekSchedule.builder()
                        .staff(staff)
                        .dayOfWeek(dto.getDayOfWeek())
                        .startTime(dto.getStartTime())
                        .endTime(dto.getEndTime())
                        .isHoliday(dto.getIsHoliday())
                        .build()

                );

            }


        }


        updateFutureWorkHistory(staff);

    }




    /*
     * 미래 근무이력 수정 + 추가
     */
    @Transactional
    /**
     * [메서드 흐름] updateFutureWorkHistory
     * Controller 또는 상위 서비스에서 호출되어 WeekScheduleRepository, WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void updateFutureWorkHistory(
            Staff staff
    ) {



        LocalDate today = LocalDate.now();



        LocalDate endDate =
                today.withDayOfMonth(
                        today.lengthOfMonth()
                );



        List<WorkHistory> histories =
                workHistoryRepository
                .findByStaffAndWorkDateBetweenOrderByWorkDateAsc(
                        staff,
                        today,
                        endDate
                );



        List<WeekSchedule> schedules =
                weekScheduleRepository
                .findByStaffOrderByDayOfWeek(staff);





        // 기존 근무 수정
        for(WorkHistory history : histories) {


            for(WeekSchedule schedule : schedules) {



                if(history.getDayOfWeek()
                        != schedule.getDayOfWeek()) {

                    continue;

                }



                history.update(
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getIsHoliday()
                );


                break;

            }

        }



        // 새로 추가된 요일 근무 생성
        generateWorkHistory(staff);


    }




    /**
     * [메서드 흐름] getWeekSchedule
     * Controller 또는 상위 서비스에서 호출되어 WeekScheduleRepository, WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<WeekScheduleResponseDTO> getWeekSchedule(
            Integer staffId
    ) {


        Staff staff = getStaff(staffId);



        return weekScheduleRepository
                .findByStaffOrderByDayOfWeek(staff)
                .stream()
                .map(schedule ->
                        WeekScheduleResponseDTO.builder()
                                .scheduleId(
                                        schedule.getId()
                                )
                                .dayOfWeek(
                                        schedule.getDayOfWeek()
                                )
                                .startTime(
                                        schedule.getStartTime()
                                )
                                .endTime(
                                        schedule.getEndTime()
                                )
                                .isHoliday(
                                        schedule.getIsHoliday()
                                )
                                .build()
                )
                .toList();

    }




    @Transactional
    /**
     * [메서드 흐름] generateCurrentMonthHistory
     * Controller 또는 상위 서비스에서 호출되어 WeekScheduleRepository, WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public void generateCurrentMonthHistory(
            Integer staffId
    ) {


        Staff staff = getStaff(staffId);


        generateWorkHistory(staff);

    }
    
    /**
     * [메서드 흐름] getStoreSchedule
     * Controller 또는 상위 서비스에서 호출되어 WeekScheduleRepository, WorkHistoryRepository, StaffRepository을 사용해 검증·조회·저장 등의 처리를 수행하고 결과를 반환한다.
     */
    public List<StoreWeekScheduleResponseDTO> getStoreSchedule(
            Integer storeId
    ){


        return weekScheduleRepository
                .findByStaff_Store_IdAndStaff_StatusNot(
                        storeId,
                        StaffStatus.TERMINATED
                )
                .stream()
                .map(schedule ->

                    StoreWeekScheduleResponseDTO.builder()

                        .dayOfWeek(
                                schedule.getDayOfWeek()
                        )

                        .staffId(
                                schedule.getStaff().getId()
                        )

                        .staffName(
                                schedule.getStaff().getName()
                        )

                        .startTime(
                                schedule.getStartTime()
                                .toString()
                        )

                        .endTime(
                                schedule.getEndTime()
                                .toString()
                        )

                        .build()

                )
                .toList();

    }


}