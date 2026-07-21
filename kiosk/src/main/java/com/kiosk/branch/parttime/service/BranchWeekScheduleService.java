package com.kiosk.branch.parttime.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kiosk.branch.kiosk.repository.StoreRepository;
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


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchWeekScheduleService {


    private final WeekScheduleRepository weekScheduleRepository;

    private final WorkHistoryRepository workHistoryRepository;

    private final StaffRepository staffRepository;

    private final StoreRepository storeRepository;



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
    public void generateCurrentMonthHistory(
            Integer staffId
    ) {


        Staff staff = getStaff(staffId);


        generateWorkHistory(staff);

    }
    
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