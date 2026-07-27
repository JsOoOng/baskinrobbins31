package com.kiosk.headquarter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.dashboard.HeadDashboardResponse;
import com.kiosk.headquarter.service.HeadDashboardService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadDashboardController
 *
 * <p>역할: 본사 관리의 대시보드 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/dashboard) -> HeadDashboardService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/dashboard")
@RequiredArgsConstructor
public class HeadDashboardController {

    private final HeadDashboardService headDashboardService;

    /**
     * [요청 흐름] GET /head/dashboard/summary
     * 프론트 요청을 받아 getDashboardSummary() 메서드가 입력을 받고 HeadDashboardService 호출 후 결과를 응답한다.
     */
    @GetMapping("/summary")
    public ResponseEntity<HeadDashboardResponse> getDashboardSummary(
            @RequestParam(required = false, defaultValue = "전일 대비") String comparisonPeriod) {
        
        return ResponseEntity.ok(headDashboardService.getDashboardSummary(comparisonPeriod));
    }
}
