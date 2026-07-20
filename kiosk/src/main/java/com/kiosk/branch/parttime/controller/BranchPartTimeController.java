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

@RestController
@RequestMapping("/branch/parttime")
@RequiredArgsConstructor
public class BranchPartTimeController {

    private final BranchStaffService staffService;
    private final BranchWeekScheduleService weekScheduleService;
    private final BranchWorkHistoryService workHistoryService;

    @PostMapping("/stores/{storeId}/staff")
    public void createStaff(
            @PathVariable Integer storeId,
            @RequestBody StaffCreateRequestDTO requestDTO
    ) {
        staffService.createStaff(storeId, requestDTO);
    }
    
    @GetMapping("/stores/{storeId}/staff")
    public List<StaffListResponseDTO> getStaffList(
            @PathVariable Integer storeId
    ) {
        return staffService.getStaffList(storeId);
    }
    
    @GetMapping("/staff/{staffId}")
    public StaffDetailResponseDTO getStaff(
            @PathVariable Integer staffId
    ) {
        return staffService.getStaff(staffId);
    }
    
    @PutMapping("/staff/{staffId}")
    public void updateStaff(
            @PathVariable Integer staffId,
            @RequestBody StaffUpdateRequestDTO requestDTO
    ) {
        staffService.updateStaff(staffId, requestDTO);
    }
    
    @PatchMapping("/staff/{staffId}/status")
    public void changeStatus(
            @PathVariable Integer staffId,
            @RequestParam StaffStatus status
    ) {
        staffService.changeStatus(staffId, status);
    }
    
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
    
    @GetMapping("/staff/{staffId}/schedule")
    public List<WeekScheduleResponseDTO> getWeekSchedule(
            @PathVariable Integer staffId
    ) {

        return weekScheduleService.getWeekSchedule(
                staffId
        );

    }
    
    @GetMapping("/stores/{storeId}/schedule")
    public List<StoreWeekScheduleResponseDTO> getStoreSchedule(

            @PathVariable Integer storeId

    ){

        return weekScheduleService
                .getStoreSchedule(storeId);

    }
    
   
}