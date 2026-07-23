package com.kiosk.branch.parttime.controller;


import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.branch.parttime.dto.HolidayUpdateRequestDTO;
import com.kiosk.branch.parttime.dto.StaffCreateRequestDTO;
import com.kiosk.branch.parttime.dto.StaffDetailResponseDTO;
import com.kiosk.branch.parttime.dto.StaffListResponseDTO;
import com.kiosk.branch.parttime.dto.StaffUpdateRequestDTO;
import com.kiosk.branch.parttime.dto.StoreWeekScheduleResponseDTO;
import com.kiosk.branch.parttime.dto.WeekScheduleResponseDTO;
import com.kiosk.branch.parttime.dto.WeekScheduleSaveRequestDTO;
import com.kiosk.branch.parttime.dto.WorkHistoryResponseDTO;
import com.kiosk.branch.parttime.dto.WorkHistoryUpdateRequestDTO;
import com.kiosk.branch.parttime.service.BranchStaffService;
import com.kiosk.branch.parttime.service.BranchWeekScheduleService;
import com.kiosk.branch.parttime.service.BranchWorkHistoryService;
import com.kiosk.entity.enums.StaffStatus;
import com.kiosk.entity.enums.WorkStatus;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] BranchPartTimeController
 *
 * <p>역할: 지점 운영의 아르바이트 직원 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/branch/parttime) -> BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/branch/parttime")
@RequiredArgsConstructor
public class BranchPartTimeController {

    private final BranchStaffService staffService;
    private final BranchWeekScheduleService weekScheduleService;
    private final BranchWorkHistoryService workHistoryService;

    /**
     * [요청 흐름] POST /branch/parttime/stores/{storeId}/staff
     * 프론트 요청을 받아 createStaff() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @PostMapping("/stores/{storeId}/staff")
    public void createStaff(
            @PathVariable Integer storeId,
            @RequestBody StaffCreateRequestDTO requestDTO
    ) {
        staffService.createStaff(storeId, requestDTO);
    }
    
    /**
     * [요청 흐름] GET /branch/parttime/stores/{storeId}/staff
     * 프론트 요청을 받아 getStaffList() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @GetMapping("/stores/{storeId}/staff")
    public List<StaffListResponseDTO> getStaffList(
            @PathVariable Integer storeId
    ) {
        return staffService.getStaffList(storeId);
    }
    
    /**
     * [요청 흐름] GET /branch/parttime/staff/{staffId}
     * 프론트 요청을 받아 getStaff() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @GetMapping("/staff/{staffId}")
    public StaffDetailResponseDTO getStaff(
            @PathVariable Integer staffId
    ) {
        return staffService.getStaff(staffId);
    }
    
    /**
     * [요청 흐름] PUT /branch/parttime/staff/{staffId}
     * 프론트 요청을 받아 updateStaff() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @PutMapping("/staff/{staffId}")
    public void updateStaff(
            @PathVariable Integer staffId,
            @RequestBody StaffUpdateRequestDTO requestDTO
    ) {
        staffService.updateStaff(staffId, requestDTO);
    }
    
    /**
     * [요청 흐름] PATCH /branch/parttime/staff/{staffId}/status
     * 프론트 요청을 받아 changeStatus() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/staff/{staffId}/status")
    public void changeStatus(
            @PathVariable Integer staffId,
            @RequestParam StaffStatus status
    ) {
        staffService.changeStatus(staffId, status);
    }
    
    /**
     * [요청 흐름] POST /branch/parttime/staff/{staffId}/schedule
     * 프론트 요청을 받아 saveWeekSchedule() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @PostMapping("/staff/{staffId}/schedule")
    public void saveWeekSchedule(
            @PathVariable Integer staffId,
            @RequestBody WeekScheduleSaveRequestDTO requestDTO
    ) {
        weekScheduleService.saveWeekSchedule(
                staffId,
                requestDTO
        );
    }
    
    /**
     * [요청 흐름] PUT /branch/parttime/staff/{staffId}/schedule
     * 프론트 요청을 받아 updateWeekSchedule() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @PutMapping("/staff/{staffId}/schedule")
    public void updateWeekSchedule(
            @PathVariable Integer staffId,
            @RequestBody WeekScheduleSaveRequestDTO requestDTO
    ) {
        weekScheduleService.updateWeekSchedule(
                staffId,
                requestDTO
        );
    }
    
    /**
     * [요청 흐름] GET /branch/parttime/staff/{staffId}/history
     * 프론트 요청을 받아 getHistory() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @GetMapping("/staff/{staffId}/history")
    public List<WorkHistoryResponseDTO> getHistory(
            @PathVariable Integer staffId,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return workHistoryService.getMonthHistory(
                staffId,
                year,
                month
        );
    }
    
    /**
     * [요청 흐름] PUT /branch/parttime/history/{historyId}
     * 프론트 요청을 받아 updateHistory() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @PutMapping("/history/{historyId}")
    public void updateHistory(
            @PathVariable Integer historyId,
            @RequestBody WorkHistoryUpdateRequestDTO requestDTO
    ) {
        workHistoryService.updateHistory(
                historyId,
                requestDTO
        );
    }
    
    /**
     * [요청 흐름] PATCH /branch/parttime/history/{historyId}/status
     * 프론트 요청을 받아 changeWorkStatus() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/history/{historyId}/status")
    public void changeWorkStatus(
            @PathVariable Integer historyId,
            @RequestParam WorkStatus status
    ) {
        workHistoryService.changeWorkStatus(
                historyId,
                status
        );
    }
    
    /**
     * [요청 흐름] PATCH /branch/parttime/history/{historyId}/holiday
     * 프론트 요청을 받아 changeHoliday() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @PatchMapping("/history/{historyId}/holiday")
    public void changeHoliday(
            @PathVariable Integer historyId,
            @RequestBody HolidayUpdateRequestDTO requestDTO
    ) {

        workHistoryService.changeHoliday(
                historyId,
                requestDTO.getIsHoliday()
        );

    }
    
    /**
     * [요청 흐름] GET /branch/parttime/staff/{staffId}/schedule
     * 프론트 요청을 받아 getWeekSchedule() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @GetMapping("/staff/{staffId}/schedule")
    public List<WeekScheduleResponseDTO> getWeekSchedule(
            @PathVariable Integer staffId
    ) {

        return weekScheduleService.getWeekSchedule(
                staffId
        );

    }
    
    /**
     * [요청 흐름] GET /branch/parttime/stores/{storeId}/schedule
     * 프론트 요청을 받아 getStoreSchedule() 메서드가 입력을 받고 BranchStaffService, BranchWeekScheduleService, BranchWorkHistoryService 호출 후 결과를 응답한다.
     */
    @GetMapping("/stores/{storeId}/schedule")
    public List<StoreWeekScheduleResponseDTO> getStoreSchedule(

            @PathVariable Integer storeId

    ){

        return weekScheduleService
                .getStoreSchedule(storeId);

    }
    
   
}