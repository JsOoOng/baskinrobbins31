package com.kiosk.headquarter.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.admin.AdminLogResponseDto;
import com.kiosk.headquarter.service.AdminLogService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] AdminLogController
 *
 * <p>역할: 본사 관리의 본사 관리자 작업 로그 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러 -> AdminLogService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequiredArgsConstructor
public class AdminLogController {

    private final AdminLogService adminLogService;

    // 관리자 작업 내역 (최신 100건) 조회 API
    /**
     * [요청 흐름] GET /head/logs
     * 프론트 요청을 받아 getRecentLogs() 메서드가 입력을 받고 AdminLogService 호출 후 결과를 응답한다.
     */
    @GetMapping("/head/logs")
    public List<AdminLogResponseDto> getRecentLogs() {
        return adminLogService.getRecentLogs();
    }
}
