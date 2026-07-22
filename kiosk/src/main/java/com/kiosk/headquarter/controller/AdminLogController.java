package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.admin.AdminLogResponseDto;
import com.kiosk.headquarter.service.AdminLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminLogController {

    private final AdminLogService adminLogService;

    // 관리자 작업 내역 (최신 100건) 조회 API
    @GetMapping("/head/logs")
    public List<AdminLogResponseDto> getRecentLogs() {
        return adminLogService.getRecentLogs();
    }
}
