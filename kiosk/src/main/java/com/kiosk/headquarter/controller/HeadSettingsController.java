package com.kiosk.headquarter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kiosk.headquarter.dto.SystemSettingDto;
import com.kiosk.headquarter.service.HeadSettingsService;

import lombok.RequiredArgsConstructor;

/**
 * [코드 흐름 안내] HeadSettingsController
 *
 * <p>역할: 본사 관리의 환경 설정 HTTP 요청을 받는 진입점이다.</p>
 * <p>호출 흐름: Vue/API 요청 -> 이 컨트롤러(/head/settings) -> HeadSettingsService -> 응답 DTO 또는 JSON -> 화면 갱신 순서로 이동한다.</p>
 * <p>데이터 기준: 제공된 SQL 초안보다 현재 Entity·Repository/Mapper·DTO 정의를 우선한다.</p>
 */
@RestController
@RequestMapping("/head/settings")
@RequiredArgsConstructor
public class HeadSettingsController {

    private final HeadSettingsService headSettingsService;

    /**
     * [요청 흐름] GET /head/settings
     * 프론트 요청을 받아 getSettings() 메서드가 입력을 받고 HeadSettingsService 호출 후 결과를 응답한다.
     */
    @GetMapping
    public ResponseEntity<SystemSettingDto> getSettings() {
        return ResponseEntity.ok(headSettingsService.getSettings());
    }

    /**
     * [요청 흐름] PUT /head/settings
     * 프론트 요청을 받아 updateSettings() 메서드가 입력을 받고 HeadSettingsService 호출 후 결과를 응답한다.
     */
    @PutMapping
    public ResponseEntity<SystemSettingDto> updateSettings(@RequestBody SystemSettingDto dto) {
        return ResponseEntity.ok(headSettingsService.updateSettings(dto));
    }
}
