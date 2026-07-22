package com.kiosk.headquarter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.dashboard.HeadDashboardResponse;
import com.kiosk.headquarter.service.HeadDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/head/dashboard")
@RequiredArgsConstructor
public class HeadDashboardController {

    private final HeadDashboardService headDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<HeadDashboardResponse> getDashboardSummary(
            @RequestParam(required = false, defaultValue = "전일 대비") String comparisonPeriod) {
        
        return ResponseEntity.ok(headDashboardService.getDashboardSummary(comparisonPeriod));
    }
}
